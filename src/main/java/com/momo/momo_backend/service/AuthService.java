package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.AuthDto;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.entity.UserCredential;
import com.momo.momo_backend.repository.UserCredentialRepository;
import com.momo.momo_backend.repository.UserRepository;
import com.momo.momo_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    private String rtKey(String loginId) {
        return "RT:" + loginId;
    }

    // 회원가입
    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (credentialRepository.findByLoginId(request.getId()).isPresent()
                || userRepository.findByLoginId(request.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String encoded = passwordEncoder.encode(request.getPassword());

        User user = userRepository.save(
                User.builder()
                        .loginId(request.getId())
                        .password(encoded)
                        .nickname(request.getNickname())
                        .build()
        );

        UserCredential credential = UserCredential.builder()
                .user(user)
                .loginId(request.getId())
                .pw(encoded)
                .build();

        credentialRepository.save(credential);
    }

    // 로그인
    @Transactional // readOnly 제거 (Redis 쓰기 포함)
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        UserCredential credential = credentialRepository.findByLoginId(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        User user = credential.getUser();
        Long userNo = user.getNo();
        String loginId = credential.getLoginId();

        String accessToken = jwtTokenProvider.createAccessToken(userNo, loginId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userNo, loginId);

        // Redis에 Refresh 저장 (문자열)
        String key = rtKey(loginId);
        stringRedisTemplate.opsForValue().set(
                key,
                refreshToken,
                jwtTokenProvider.getRefreshTokenValidity(),
                TimeUnit.MILLISECONDS
        );
        log.debug("[LOGIN] Saved RT to Redis key={}, ttl(ms)={}", key, jwtTokenProvider.getRefreshTokenValidity());

        return AuthDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userNo(userNo)
                .build();
    }
    // 토큰 재발급
    @Transactional
    public AuthDto.LoginResponse refresh(String refreshTokenOrBearer) {
        if (!jwtTokenProvider.validateToken(refreshTokenOrBearer)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userNo = jwtTokenProvider.getUserNo(refreshTokenOrBearer);
        String loginId = jwtTokenProvider.getUserIdFromToken(refreshTokenOrBearer);
        if (userNo == null || loginId == null) {
            throw new IllegalArgumentException("리프레시 토큰에서 사용자 정보를 찾을 수 없습니다.");
        }

        String key = rtKey(loginId);
        String stored = stringRedisTemplate.opsForValue().get(key);
        String rawRefresh = jwtTokenProvider.resolveBearer(refreshTokenOrBearer);

        log.debug("[REFRESH] Redis get key={}, stored={}", key, (stored != null ? "present" : "null"));

        if (stored == null || !stored.equals(rawRefresh)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않거나 만료되었습니다. 다시 로그인해주세요.");
        }

        String newAccess = jwtTokenProvider.createAccessToken(userNo, loginId);
        String newRefresh = jwtTokenProvider.createRefreshToken(userNo, loginId);

        // RT 갱신
        stringRedisTemplate.opsForValue().set(
                key, newRefresh,
                jwtTokenProvider.getRefreshTokenValidity(),
                TimeUnit.MILLISECONDS
        );
        log.debug("[REFRESH] Updated RT in Redis key={}, ttl(ms)={}", key, jwtTokenProvider.getRefreshTokenValidity());

        return AuthDto.LoginResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .userNo(userNo)
                .build();
    }

    // 로그아웃
    @Transactional
    public void logout(String accessHeaderOrToken) {
        String access = jwtTokenProvider.resolveBearer(accessHeaderOrToken);
        if (access == null) return;

        Date exp = jwtTokenProvider.getExpirationDateFromToken(access);
        long remain = exp.getTime() - System.currentTimeMillis();
        if (remain > 0) {
            // Access 블랙리스트
            stringRedisTemplate.opsForValue().set(access, "logout", remain, TimeUnit.MILLISECONDS);
        }

        String loginId = jwtTokenProvider.getUserIdFromToken(access);
        if (loginId != null) {
            stringRedisTemplate.delete(rtKey(loginId)); // RT 제거
        }
    }

    // 아이디 중복 확인
    @Transactional(readOnly = true)
    public boolean checkIdExists(String id) {
        return userRepository.findByLoginId(id).isPresent()
                || credentialRepository.findByLoginId(id).isPresent();
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public boolean checkNicknameExists(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(Long userNo) {
        credentialRepository.deleteByUser_No(userNo);
        userRepository.deleteById(userNo);
    }

   // 아이디 찾기
    @Transactional(readOnly = true)
    public String findId(String nickname, String password) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("닉네임에 해당하는 사용자가 없습니다."));

        UserCredential cred = credentialRepository.findByLoginId(user.getLoginId())
                .orElseThrow(() -> new IllegalStateException("자격증명 정보를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, cred.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user.getLoginId();
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String id, String nickname, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호가 유효하지 않습니다.");
        }

        // 1) 사용자/자격증명 로드
        User user = userRepository.findByLoginId(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        if (!nickname.equals(user.getNickname())) {
            throw new IllegalArgumentException("닉네임이 일치하지 않습니다.");
        }

        UserCredential cred = credentialRepository.findByLoginId(id)
                .orElseThrow(() -> new IllegalStateException("자격증명 정보를 찾을 수 없습니다."));

        // 2) 새 비밀번호 인코딩 후 반영
        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        cred.setPw(encoded);

        // 3) 저장
        userRepository.save(user);
        credentialRepository.save(cred);

        // 4) 기존 Refresh 무효화
        stringRedisTemplate.delete(rtKey(id));
    }
}
