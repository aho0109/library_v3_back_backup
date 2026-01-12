package org.matsuzaka.library_v3_back.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 將驗證註解加入 DTO 字段，搭配 Controller 的 @Valid @RequestBody，Spring 在反序列化後會自動驗證欄位。
// 驗證失敗時會拋出 MethodArgumentNotValidException，可用 @ControllerAdvice 或繼承 ResponseEntityExceptionHandler 處理並回傳自定義 ApiResponse。

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "account 必填")
    private String account;

    @NotBlank(message = "password 必填")
    @Size(min = 8, max = 128, message = "password 長度須為 8-128 字元")
    private String password;

    @NotBlank(message = "name 必填")
    private String name;

    @Email(message = "email 格式錯誤")
    @NotBlank(message = "email 必填")
    private String email;

    @Pattern(regexp = "^[0-9\\-\\+\\s()]{7,20}$", message = "phone 格式錯誤")
    private String phone;

    @NotBlank(message = "address 必填")
    private String address;

    // cardId 將在後端自動生成，不從前端接收
    // role 也將在後端設置為預設值
}
