package com.devyu.blog.service;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devyu.blog.constant.Constant;
import com.devyu.blog.domain.Blog;
import com.devyu.blog.domain.Tag;
import com.devyu.blog.domain.User;
import com.devyu.blog.inputForm.BlogForm;
import com.devyu.blog.repository.BlogRepository;
import com.devyu.blog.repository.TagRepository;
import com.devyu.blog.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogService {
	
	private final BlogRepository blogRepository;
	private final UserRepository userRepository;
	private final TagRepository tagRepository;
	
	@Transactional
	public Blog create(BlogForm blogForm, HttpSession session) {

		// session에 담아둔 login user의 정보 가져오기
		User user = (User)session.getAttribute(Constant.SESSIONED_ID);
		
		// 영속성 컨텍스트 1차캐시에 User 객체를 생성
		User userPersistence = userRepository.findOne(user.getId());
		
		Blog blog = Blog.createBlog(userPersistence, blogForm);
		
		String[] tags = blogForm.getTagName();
		for(int i=0;i<tags.length;i++) {
			if(tags[i].trim() != "") {
				Tag tag = Tag.createTag(tags[i].trim(), blog);
				
				// 태그 저장
				tagRepository.create(tag);
			}
		}
		
		// 블로그 저장
		blogRepository.create(blog);
		
		return blog;
	}
	
	public List<Blog> findAll() {
		return blogRepository.findAll();
	}

	@Transactional
	public Blog findOne(Long id, boolean isCookie) {
		Blog blog = blogRepository.findOne(id);
		if(!isCookie) {
			blog.addCountOfViews();
		}
		return blog;
	}

	@Transactional
	public Long findCountOfThumbsUp(Long id) {
		Blog blog = blogRepository.findOne(id);
		blog.addCountOfThumbsUp();
		return blog.getCountOfThumbsUp();
	}

	public List<Blog> findPopList() {
		return blogRepository.findPopList();
	}

	public List<Blog> findAllSearchText(String keyword) {
		return blogRepository.findAllSearchText(keyword);
	}

	public int findAllCnt() {
		return blogRepository.findAllCnt();
	}

	public List<Blog> findListPaging(int startIndex, int pageSize) {
		return blogRepository.findListPaging(startIndex, pageSize);
	}

	public int findAllForTagNameCnt(String tagName) {
		return blogRepository.findAllForTagNameCnt(tagName);
	}

	public int findAllForSearchCnt(String search) {
		return blogRepository.findAllForSearchCnt(search);
	}

	public List<Blog> findListPagingForTagName(String tagName, int startIndex, int pageSize) {
		return blogRepository.findListPagingForTagName(tagName, startIndex, pageSize);
	}

	public List<Blog> findListPagingForSearch(String search, int startIndex, int pageSize) {
		return blogRepository.findListPagingForSearch(search, startIndex, pageSize);
	}

	public Blog findOne(Long id) {
		return blogRepository.findOne(id);
	}

	@Transactional
	public void update(Long id, BlogForm blogForm) {
		
		Blog blog = blogRepository.findOne(id);

		// 태그 삭제
		for(int i=0;i<blog.getBlogTags().size();i++) {
			Tag tag = blog.getBlogTags().get(i).getTag();
			tagRepository.delete(tag);
		} 

		// 블로그 업데이트
		blog.update(blogForm);

		// 태그 추가
		String[] tags = blogForm.getTagName();
		for(int i=0;i<tags.length;i++) {
			if(tags[i].trim() != "") {
				Tag tag = Tag.createTag(tags[i].trim(), blog);
				// 태그 저장
				tagRepository.create(tag);
			}
		}
	}

	@Transactional
	public void delete(Long id) {
		Blog blog = blogRepository.findOne(id);
		blogRepository.delete(blog);
	}
}
