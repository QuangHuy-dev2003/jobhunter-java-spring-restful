package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.domain.Payment;
import vn.hoidanit.jobhunter.domain.PostLimit;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.postlimit.PostLimitDTO;
import vn.hoidanit.jobhunter.repository.PaymentRepository;
import vn.hoidanit.jobhunter.repository.PostLimitRepository;
import vn.hoidanit.jobhunter.repository.SubscriptionRepository;

@Service
public class PostLimitService {
  private final PostLimitRepository postLimitRepository;
  private final PaymentRepository paymentRepository;
  private final SubscriptionRepository subscriptionRepository;

  public PostLimitService(
      PostLimitRepository postLimitRepository,
      PaymentRepository paymentRepository,
      SubscriptionRepository subscriptionRepository) {
    this.postLimitRepository = postLimitRepository;
    this.paymentRepository = paymentRepository;
    this.subscriptionRepository = subscriptionRepository;
  }

  public Optional<PostLimit> getPostLimitById(long id) {
    return postLimitRepository.findById(id);
  }



  public PostLimit handleCreatePostLimit(PostLimit postLimit) {
    return postLimitRepository.save(postLimit);
  }
  // delete postlimit
  @Transactional
  public void handleDeletePostLimit(long id) {
    // Xóa tất cả subscriptions liên quan
    List<Subscription> subscriptions = subscriptionRepository.findByPostLimitId(id);
    subscriptionRepository.deleteAll(subscriptions);

    // Xóa tất cả payments liên quan
    List<Payment> payments = paymentRepository.findByPostLimitId(id);
    paymentRepository.deleteAll(payments);

    // xóa post limit
    postLimitRepository.deleteById(id);
  }

  public ResultPaginationDTO getPostLimitsWithPagination(Pageable pageable) {
    Page<PostLimit> pagePostLimits = postLimitRepository.findAll(pageable);

    ResultPaginationDTO rs = new ResultPaginationDTO();
    ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

    mt.setPage(pageable.getPageNumber() + 1); // Page bắt đầu từ 0, nên cần +1
    mt.setPageSize(pageable.getPageSize());
    mt.setPages(pagePostLimits.getTotalPages());
    mt.setTotal(pagePostLimits.getTotalElements());

    rs.setMeta(mt);

    // Chuyển đổi dữ liệu nếu cần thiết
    List<PostLimitDTO> listPostLimit = pagePostLimits.getContent()
        .stream()
        .map(this::convertToPostLimitDTO) // Phương thức này chuyển đổi PostLimit sang DTO
        .collect(Collectors.toList());

    rs.setResult(listPostLimit);

    return rs;
  }

  private PostLimitDTO convertToPostLimitDTO(PostLimit postLimit) {
    // Thay đổi logic chuyển đổi theo nhu cầu của bạn
    PostLimitDTO dto = new PostLimitDTO();
    dto.setId(postLimit.getId());
    dto.setPlanName(postLimit.getPlanName());
    dto.setPrice(postLimit.getPrice());
    dto.setMaxPostsPerMonth(postLimit.getMaxPostsPerMonth());
    dto.setDescription(postLimit.getDescription());
    return dto;
  }


}
