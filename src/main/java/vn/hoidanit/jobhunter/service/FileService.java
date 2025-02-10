package vn.hoidanit.jobhunter.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  @Value("${hoidanit.upload-file.base-uri}")
  private String baseURI;

//    public void createDirectory(String folder) throws URISyntaxException {
//        URI uri = new URI(folder);
//        Path path = Paths.get(uri);
//        File tmpDir = new File(path.toString());
//        if (!tmpDir.isDirectory()) {
//            try {
//                Files.createDirectory(tmpDir.toPath());
//                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + tmpDir.toPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
//        }
//
//    }

  // Phương thức helper để chuyển URI string thành Path
  private Path getPathFromUri(String... paths) {
    try {
      // Loại bỏ "file:///" từ baseURI nếu có
      String basePath = baseURI.replace("file:///", "");
      // Nối các phần đường dẫn lại
      return Paths.get(basePath, paths);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Không thể chuyển đổi URI thành Path", e);
    }
  }

    public void createDirectory(String folder) {
        Path dirPath = getPathFromUri(folder);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
                System.out.println(">>> TẠO THƯ MỤC MỚI THÀNH CÔNG, ĐƯỜNG DẪN = " + dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> BỎ QUA TẠO THƯ MỤC, ĐÃ TỒN TẠI");
        }
    }


    public String store(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename().replaceAll("\\s+", "_");
        String finalName = System.currentTimeMillis() + "-" + originalFilename;

        Path uploadPath = getPathFromUri(folder);
        Path filePath = uploadPath.resolve(finalName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

//    public long getFileLength(String fileName, String folder) throws URISyntaxException {
//        URI uri = new URI(baseURI + folder + "/" + fileName);
//        Path path = Paths.get(uri);
//
//        File tmpDir = new File(path.toString());
//
//        // file không tồn tại, hoặc file là 1 director => return 0
//        if (!tmpDir.exists() || tmpDir.isDirectory())
//            return 0;
//        return tmpDir.length();
//    }
//
//    public InputStreamResource getResource(String fileName, String folder)
//            throws URISyntaxException, FileNotFoundException {
//        URI uri = new URI(baseURI + folder + "/" + fileName);
//        Path path = Paths.get(uri);
//
//        File file = new File(path.toString());
//        return new InputStreamResource(new FileInputStream(file));
//    }

    public long getFileLength(String fileName, String folder) {
        Path filePath = getPathFromUri(folder, fileName);
        File file = filePath.toFile();
        if (!file.exists() || file.isDirectory()) {
            return 0;
        }
        return file.length();
    }

    public InputStreamResource getResource(String fileName, String folder)
        throws FileNotFoundException {
        Path filePath = getPathFromUri(folder, fileName);
        File file = filePath.toFile();
        return new InputStreamResource(new FileInputStream(file));
    }
}
