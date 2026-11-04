-- 개발용 기본 데이터. application-dev.yml 의 spring.sql.init.mode=always 일 때만 실행된다.
-- ponytail: 캐릭터 가격·이미지는 미정이라 임시값. 확정되면 여기만 고친다.
INSERT IGNORE INTO characters (id, name, image_url, price) VALUES
  (1, '기본 다람쥐', 'https://placehold.co/200x200?text=squirrel', 0),
  (2, '부자 다람쥐', 'https://placehold.co/200x200?text=rich', 50);

INSERT IGNORE INTO badges (id, name, image_url) VALUES
  (1, '첫 단계 완수', NULL);
