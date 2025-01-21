package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.PostLimit;
import vn.hoidanit.jobhunter.repository.PostLimitRepository;

@Service
public class PostLimitService {
    private final PostLimitRepository postLimitRepository;

    public PostLimitService(PostLimitRepository postLimitRepository) {
        this.postLimitRepository = postLimitRepository;
    }

    public Optional<PostLimit> getPostLimitById(long id) {
        return postLimitRepository.findById(id);
    }

    public List<PostLimit> getAllPostLimits() {
        return postLimitRepository.findAll();
    }

    public PostLimit handleCreatePostLimit(PostLimit postLimit) {
        return postLimitRepository.save(postLimit);
    }

    public void handleDeletePostLimit(long id) {
        postLimitRepository.deleteById(id);
    }

}
