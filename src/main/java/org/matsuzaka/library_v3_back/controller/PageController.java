package org.matsuzaka.library_v3_back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin(origins = "*")
public class PageController {

     // 根目錄 "/" 會自動對應到 static/index.html，所以這個方法可以省略
     @GetMapping("/")
     public String index() {
         return "forward:/index.html";
     }

    // forward:/some/path 的意思是告訴 DispatcherServlet：
    // 「請不要使用視圖解析器，而是將這個請求在伺服器內部直接轉發到 /some/path 這個 URL 路徑上」。
    // 因為 /bookQuery.html 是一個有效的靜態資源 URL，所以這個請求會被正確處理。

    @GetMapping("/bookQuery")
    public String books() {
        // 伺服器內部轉發到 /bookQuery.html 這個靜態資源 URL
        // return "forward:/bookQuery.html"; // 可行
        // return "/bookQuery.html"; // 直接返回靜態資源的路徑，這樣也可以
        return "/bookQuery.html"; // 沒在前面加斜線，這樣會被視為相對路徑，但也可以
    }

    @GetMapping("/oneBook")
    public String oneBook() {
        return "/oneBook.html"; // 對應 oneBook.html
    }

    @GetMapping("/html/register")
    public String register() {
        return "/html/register.html"; // 對應 register.html
    }

    // 將 @GetMapping 的路徑改成和前端 href 一致
    @GetMapping("/html/myPage")
    public String myPage() {
        // 轉發到 static 資料夾下的正確檔案路徑
        return "/html/myPage.html";
    }

    @GetMapping("/html/admin/adminPanel")
    public String adminPanel() {
        return "/html/admin/adminPanel.html"; // 對應 myPage.html
    }

    @GetMapping("/html/admin/adminCreateBook")
    public String adminCreateBook() {
        return "/html/admin/adminCreateBook.html"; // 對應 myPage.html
    }

}