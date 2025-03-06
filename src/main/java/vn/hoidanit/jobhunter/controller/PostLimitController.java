package vn.hoidanit.jobhunter.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
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
import vn.hoidanit.jobhunter.domain.PostLimit;
import vn.hoidanit.jobhunter.domain.response.ResponseDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.PostLimitService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class PostLimitController {
  private final PostLimitService postLimitService;

  public PostLimitController(PostLimitService postLimitService) {
    this.postLimitService = postLimitService;
  }
  @GetMapping("/post-limits")
  @ApiMessage("Get all post limits with pagination")
  public ResponseEntity<ResultPaginationDTO> getAllPostLimits(
      Pageable pageable) {

    return ResponseEntity.status(HttpStatus.OK).body(
        this.postLimitService
            .getPostLimitsWithPagination(pageable));
  }


  @PostMapping("/post-limits")
  @ApiMessage("Create a new post limit")
  public ResponseEntity<PostLimit> createNewPostLimit(@Valid @RequestBody PostLimit postLimit) {
    return ResponseEntity.status(HttpStatus.CREATED).body(postLimitService.handleCreatePostLimit(postLimit));
  }

  @DeleteMapping("/post-limits/{id}")
  @ApiMessage("Delete a post limit")
  public ResponseEntity<Void> deletePostLimit(@PathVariable long id) throws IdInvalidException {
    if (postLimitService.getPostLimitById(id).isEmpty()) {
      throw new IdInvalidException("Post limit với id = " + id + " không tồn tại");
    }
    postLimitService.handleDeletePostLimit(id);
    return ResponseEntity.ok(null);
  }

  @PutMapping("/post-limits/{id}")
  @ApiMessage("Update a post limit")
  public ResponseEntity<PostLimit> updatePostLimit(@PathVariable long id, @Valid @RequestBody PostLimit postLimit)
      throws IdInvalidException{
    if (postLimitService.getPostLimitById(id).isEmpty()) {
      throw new IdInvalidException("Post limit với id = " + id + " không tồn tại");
    }
    postLimit.setId(id);
    return ResponseEntity.ok(postLimitService.handleCreatePostLimit(postLimit));
  }


}
