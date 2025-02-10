package vn.hoidanit.jobhunter.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class WebScraperController {

  @GetMapping("/web-scraper")
  @ApiMessage("Scrape a blog")
  public ResponseEntity<Map<String, Object>> scrapeBlog(@RequestParam String url) {
    Map<String, Object> response = new HashMap<>();
    try {
      // Kết nối đến URL và lấy nội dung
      Document document = Jsoup.connect(url)
          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36")
          .timeout(10000) // Tăng thời gian chờ
          .referrer("https://www.google.com") // Referrer hợp lệ
          .header("Accept-Language", "en-US,en;q=0.9")
          .header("Accept-Encoding", "gzip, deflate, br")
          .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
          .get();


      // Lấy tiêu đề (title)
      String title = document.title();

      // Lấy thẻ h1 đầu tiên
      Element h1Element = document.selectFirst("h1");
      String h1 = h1Element != null ? h1Element.text() : null;

      // Lấy hình ảnh đầu tiên (thẻ <img>)
      Element imgElement = document.selectFirst("img");
      String image = imgElement != null ? imgElement.absUrl("src") : null;

      // Chuẩn bị phản hồi
      response.put("title", title);
      response.put("h1", h1);
      response.put("image", image);
      response.put("url", url);

      return ResponseEntity.ok(response);

    } catch (IOException e) {
      response.put("error", "Không thể tải URL: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}