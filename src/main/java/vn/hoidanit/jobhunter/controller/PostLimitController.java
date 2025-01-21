package vn.hoidanit.jobhunter.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.Util.Error.IdInvalidException;
import vn.hoidanit.jobhunter.Util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.domain.PostLimit;
import vn.hoidanit.jobhunter.service.PostLimitService;


@RestController
@RequestMapping("/api/v1")
public class PostLimitController {
    private final PostLimitService postLimitService;

    public PostLimitController(PostLimitService postLimitService) {
        this.postLimitService = postLimitService;
    }

    @GetMapping("/post-limits")
    @ApiMessage("Get all post limits")
    public ResponseEntity<List<PostLimit>> getAllPostLimits() {
        List<PostLimit> postLimits = postLimitService.getAllPostLimits();
        return ResponseEntity.ok(postLimits);
    }

    @PostMapping("/post-limits")
    @ApiMessage("Create a new post limit")
    public ResponseEntity<PostLimit> createNewPostLimit(@Valid @RequestBody PostLimit postLimit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postLimitService.handleCreatePostLimit(postLimit));
    }

    @DeleteMapping("/post-limits/{id}")
    @ApiMessage("Delete a post limit")
    public ResponseEntity<String> deletePostLimit(@PathVariable long id) throws IdInvalidException {
        if (postLimitService.getPostLimitById(id).isEmpty()) {
            throw new IdInvalidException("Post limit với id = " + id + " không tồn tại");
        }
        postLimitService.handleDeletePostLimit(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @PutMapping("/post-limits/{id}")
    @ApiMessage("Update a post limit")
    public ResponseEntity<PostLimit> updatePostLimit(@PathVariable long id, @Valid @RequestBody PostLimit postLimit)
            throws IdInvalidException {
        if (postLimitService.getPostLimitById(id).isEmpty()) {
            throw new IdInvalidException("Post limit với id = " + id + " không tồn tại");
        }
        postLimit.setId(id);
        return ResponseEntity.ok(postLimitService.handleCreatePostLimit(postLimit));
    }

}