package org.matsuzaka.library_v3_back.dto.adminDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManyToManyInputDTO { // 輔助 DTO，用於接收多對多關聯的輸入，目前是作者群和標籤群

    // 前端中從表單選取的 ID 列表，可選顧名思義是已存在的，後續會把新書和這裡已存在的關聯連上
    private List<Long> existingIds = new ArrayList<>();
    // 前端中新增的名稱列表，這些名稱會被轉換為新的實體並保存到資料庫
    private List<String> newNames = new ArrayList<>();

}
