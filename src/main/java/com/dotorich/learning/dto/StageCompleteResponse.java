package com.dotorich.learning.dto;

/**
 * 단계 완수 응답.
 * dailyRewarded: 오늘 첫 하루 학습이라 보상을 받았는지 (추가 풀이면 false)
 * acornEarned: 이번에 받은 도토리 전체 (나무 수확 시 1+5=6, 추가 풀이면 0)
 * tree: 지금 키우는 나무 (이번에 수확했으면 새로 시작한 나무)
 * treeCompleted: 이번에 나무를 수확했는지
 * treeCount: 내 숲 나무 수 (이번 수확 포함, 홈의 tree.treeCount 와 같은 뜻). 수확 화면의 "N번째 나무"
 * treeBonus: 이번 수확 보너스 도토리 (수확 안 했으면 0). acornEarned 에 이미 포함
 * newTermCount: 이번 단계 용어 수
 * cardReviewCount: 다음 학습 전 카드뉴스로 복습할 문제 수
 * nextStageId: 같은 섹터 다음 단계 (마지막 단계면 null)
 */
public record StageCompleteResponse(
		boolean dailyRewarded,
		int acornEarned,
		int acornBalance,
		int streak,
		TreeInfo tree,
		boolean treeCompleted,
		int treeCount,
		int treeBonus,
		int newTermCount,
		long cardReviewCount,
		Long nextStageId
) {

	public record TreeInfo(int filledCount, int totalCount, int fallenCount) {
	}
}
