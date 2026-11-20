package com.dotorich.newsquiz.dto;

public record NewsQuizAnswerResponse(boolean correct, String correctAnswer, String explanation) {
}
