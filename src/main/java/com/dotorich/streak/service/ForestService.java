package com.dotorich.streak.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dotorich.streak.dto.ForestResponse;
import com.dotorich.streak.dto.ForestResponse.TreeItem;
import com.dotorich.streak.repository.TreeRepository;

import lombok.RequiredArgsConstructor;

/** 내 숲 조회 (P2). 나무 등록은 단계 완료에서 7칸이 될 때 자동으로 처리된다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForestService {

	private final TreeRepository treeRepository;

	public ForestResponse getForest(Long userId) {
		List<TreeItem> trees = treeRepository
				.findAllByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDescIdDesc(userId).stream()
				.map(tree -> new TreeItem(tree.getId(), tree.getCompletedAt().toLocalDate(), tree.getFallenCount()))
				.toList();
		return new ForestResponse(trees.size(), trees);
	}
}
