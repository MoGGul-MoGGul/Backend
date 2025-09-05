package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.ProfileDto;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final S3UploadService s3UploadService; // S3UploadService 주입

    // 프로필 수정 메서드
    @Transactional
    public ProfileDto.UpdateResponse updateProfile(Long userNo, String nickname, MultipartFile imageFile) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 닉네임 수정 로직 (닉네임이 요청에 포함된 경우)
        if (nickname != null && !nickname.isBlank()) {
            if (!user.getNickname().equals(nickname) && userRepository.findByNickname(nickname).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(nickname);
        }

        String newProfileImageUrl = user.getProfileImage();

        // 2. 프로필 이미지 수정 로직 (이미지 파일이 요청에 포함된 경우)
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // S3에 이미지 업로드 후 URL 반환
                newProfileImageUrl = s3UploadService.upload(imageFile, "profile");
                user.setProfileImage(newProfileImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 저장에 실패했습니다.", e);
            }
        }

        userRepository.save(user);

        return ProfileDto.UpdateResponse.builder()
                .message("프로필이 성공적으로 수정되었습니다.")
                .profileImageUrl(newProfileImageUrl)
                .build();
    }
}