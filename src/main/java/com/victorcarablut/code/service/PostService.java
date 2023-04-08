package com.victorcarablut.code.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.victorcarablut.code.dto.LikeDto;
import com.victorcarablut.code.entity.post.Like;
import com.victorcarablut.code.entity.post.Post;
import com.victorcarablut.code.entity.user.User;
import com.victorcarablut.code.exceptions.EmailNotExistsException;
import com.victorcarablut.code.exceptions.ErrorSaveDataToDatabaseException;
import com.victorcarablut.code.exceptions.GenericException;
import com.victorcarablut.code.exceptions.InvalidEmailException;
import com.victorcarablut.code.repository.LikeRepository;
import com.victorcarablut.code.repository.PostRepository;
import com.victorcarablut.code.repository.UserRepository;

@Service
public class PostService {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LikeRepository likeRepository;

	public boolean existsPostById(Long id) {
		return postRepository.existsById(id);
	}

	public boolean existsUserById(Long id) {
		return userRepository.existsById(id);
	}

	public List<Post> findAllPosts() {

		updateListPostTotalLikes();

		return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}

	public void createPost(Post post, MultipartFile image) {
		post.setCreatedDate(LocalDateTime.now());

		try {
			postRepository.save(post);
		} catch (Exception e) {
			throw new ErrorSaveDataToDatabaseException();
		}

		if (image != null) {
			if (!image.isEmpty()) {

				// post.setImage(image.getBytes());
				try {
					uploadImg(post.getUser().getId(), post.getId(), image);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void updatePost(Long postId, Post post, MultipartFile image, String imageStatus) {

		Post postUpdate = postRepository.findPostById(postId);

		// System.out.println("userId: " + post.getUser().getId());
		// System.out.println("postId: " + postId);

		// System.out.println("title: " + post.getTitle());
		// System.out.println("desc: " + post.getDescription());
		System.out.println("image: " + post.getImage());

		postUpdate.setTitle(post.getTitle());
		postUpdate.setDescription(post.getDescription());

		if (imageStatus.contains("no-image")) {
			postUpdate.setImage(null);
			System.out.println(imageStatus);
		}

		postUpdate.setUpdatedDate(LocalDateTime.now());

		try {
			postRepository.save(postUpdate);
		} catch (Exception e) {
			throw new ErrorSaveDataToDatabaseException();
		}

		if (image != null) {
			if (!image.isEmpty()) {

				// post.setImage(image.getBytes());
				try {
					uploadImg(post.getUser().getId(), postId, image);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void uploadImg(Long userId, Long postId, MultipartFile file) throws IOException {

		if (existsUserById(userId) && existsPostById(postId)) {

			byte[] decodeUserImgBase64 = null;
			try {
				decodeUserImgBase64 = file.getBytes();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			final long imgSize = decodeUserImgBase64.length;
			Long finalImgSize = 0L;
			String imgSizeFile = "NULL";
			String imgType = file.getContentType().toString();

			long kilobyte = 1024;
			long megabyte = kilobyte * 1024;
			long gigabyte = megabyte * 1024;
			long terabyte = gigabyte * 1024;

			if ((imgSize >= 0) && (imgSize < kilobyte)) {
				finalImgSize = imgSize; // B
				imgSizeFile = "B";

			} else if ((imgSize >= kilobyte) && (imgSize < megabyte)) {
				finalImgSize = (imgSize / kilobyte); // KB
				imgSizeFile = "KB";

			} else if ((imgSize >= megabyte) && (imgSize < gigabyte)) {
				finalImgSize = (imgSize / megabyte); // MB
				imgSizeFile = "MB";

			} else if ((imgSize >= gigabyte) && (imgSize < terabyte)) {
				finalImgSize = (imgSize / gigabyte); // GB
				imgSizeFile = "GB";

			} else if (imgSize >= terabyte) {
				finalImgSize = (imgSize / terabyte); // TB
				imgSizeFile = "TB";

			} else {
				finalImgSize = imgSize;
				imgSizeFile = "NULL";
			}

			System.out.println(finalImgSize + imgSizeFile + " " + imgType);

			// max: 10 MB
			if ((finalImgSize > 10 && imgSizeFile == "MB") || imgSizeFile == "GB" || imgSizeFile == "TB"
					|| imgSizeFile == "NULL" || !imgType.equals("image/jpeg")) {
				throw new GenericException();
			} else {

				Post post = postRepository.findPostById(postId);
				post.setImage(file.getBytes());
				// post.setUpdatedDate(LocalDateTime.now());

				try {
					postRepository.save(post);
				} catch (Exception e) {
					throw new ErrorSaveDataToDatabaseException();
				}
			}

		} else {
			throw new GenericException();
		}
	}

	public void deletePost(Long userId, Long postId) {

		if (existsUserById(userId) && existsPostById(postId)) {
			postRepository.deleteById(postId);

		} else {
			throw new GenericException();
		}

	}

	// ---------- Likes ----------

	public void addLike(Like like) {

		likeRepository.save(like);

		updateListPostTotalLikes();
	}

	public void removeLike(Like like) {

		//likeRepository.save(like);
		
		
		//System.out.println(like.getPost().getId());
		//System.out.println(like.getUser().getId());
		
		Like findLike =  likeRepository.findByPostIdAndUserId(like.getPost().getId(), like.getUser().getId());
		
		//System.out.println(findLike.getId());

		//likeRepository.deleteByPostIdAndUserId(postId, userId);
		
		likeRepository.deleteById(findLike.getId());

		updateListPostTotalLikes();
	}

	public List<LikeDto> findAllPostLikes(LikeDto likeDto) {

		List<Like> likes = likeRepository.findAllByPostId(likeDto.getPostId());

		List<LikeDto> list = new ArrayList<LikeDto>();

		for (Like like : likes) {
			LikeDto likeDto2 = new LikeDto();
			// System.out.println(like.getUser().getFullName());
			likeDto2.setPostId(like.getPost().getId());
			likeDto2.setUserId(like.getUser().getId());
			likeDto2.setUserFullName(like.getUser().getFullName());

			list.add(likeDto2);
		}

		return list;
	}

	public void updateListPostTotalLikes() {
		List<Post> posts = postRepository.findAll();

		for (Post post : posts) {
			// System.out.println(likeRepository.countByPostId(post.getId()));
			post.setTotalLikes(likeRepository.countByPostId(post.getId()));

			if (likeRepository.existsPostByPostIdAndUserId(post.getId(), post.getUser().getId())) {
				// System.out.println("postId: " + post.getId() + " / " + "userId: " +
				// post.getUser().getId() + " / " + "userName: " +
				// post.getUser().getFullName());
				post.setIsOwnerLike(true);
			} else {
				// System.out.println("false");
				post.setIsOwnerLike(false);
			}
			
			//System.out.println();

			postRepository.save(post);
		}
	}

}
