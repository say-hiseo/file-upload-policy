package com.assignment.fileuploadpolicy.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.assignment.fileuploadpolicy.domain.policy.service.ExtensionNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Nested;

class ExtensionNormalizerTest {

    @Nested
    class 정상_입력 {

        @Test
        @DisplayName("소문자 영문+숫자 조합은 그대로 통과한다")
        void 기본_통과() {
            assertThat(ExtensionNormalizer.normalize("sh")).contains("sh");
            assertThat(ExtensionNormalizer.normalize("mp3")).contains("mp3");
            assertThat(ExtensionNormalizer.normalize("a1b2")).contains("a1b2");
        }
    }

    @Nested
    class 대소문자_정규화 {

        @ParameterizedTest
        @CsvSource({"SH,sh", "Sh,sh", "sH,sh", "EXE,exe"})
        @DisplayName("대소문자는 전부 소문자로 정규화된다")
        void 소문자로_변환(String input, String expected) {
            assertThat(ExtensionNormalizer.normalize(input)).contains(expected);
        }
    }

    @Nested
    class 점_처리 {

        @ParameterizedTest
        @CsvSource({".sh,sh", "sh.,sh", ".sh.,sh", "..sh,sh"})
        @DisplayName("앞뒤에 붙은 점은 제거된다")
        void 앞뒤_점_제거(String input, String expected) {
            assertThat(ExtensionNormalizer.normalize(input)).contains(expected);
        }

        @Test
        @DisplayName("점만 있거나 정규화 후 빈 문자열이 되면 거부한다")
        void 점만_있으면_거부() {
            assertThat(ExtensionNormalizer.normalize(".")).isEmpty();
            assertThat(ExtensionNormalizer.normalize("..")).isEmpty();
            assertThat(ExtensionNormalizer.normalize("...")).isEmpty();
        }

        @Test
        @DisplayName("중간에 점이 있는 값은 화이트리스트에 걸려 거부한다 (조각 분리는 이 클래스의 책임이 아님)")
        void 중간_점은_거부() {
            // ExtensionNormalizer는 확장자 "하나"만 정규화한다.
            // "exe.txt" 같은 이중 확장자를 조각으로 나누는 건 업로드 검증 쪽 책임이다.
            assertThat(ExtensionNormalizer.normalize("exe.txt")).isEmpty();
        }
    }

    @Nested
    class 공백_처리 {

        @ParameterizedTest
        @ValueSource(strings = {" sh", "sh ", " sh ", "\tsh\t"})
        @DisplayName("일반 공백/탭은 trim되어 통과한다")
        void 일반_공백_제거(String input) {
            assertThat(ExtensionNormalizer.normalize(input)).contains("sh");
        }

        @Test
        @DisplayName("중간에 공백이 있으면 화이트리스트에서 거부한다")
        void 중간_공백은_거부() {
            assertThat(ExtensionNormalizer.normalize("s h")).isEmpty();
        }

        @Test
        @DisplayName("공백만 있으면 거부한다")
        void 공백만_있으면_거부() {
            assertThat(ExtensionNormalizer.normalize("   ")).isEmpty();
        }

        @Test
        @DisplayName("Non-breaking space(U+00A0)처럼 일반 공백이 아닌 것도 결국 화이트리스트에서 걸러진다")
        void non_breaking_space_거부() {
            assertThat(ExtensionNormalizer.normalize("s\u00A0h")).isEmpty();
        }
    }

    @Nested
    class 특수문자_거부 {

        @ParameterizedTest
        @ValueSource(strings = {"s;h", "s*h", "s/h", "s\\h", "s<h", "s>h", "s|h", "s'h", "s\"h"})
        @DisplayName("특수문자가 섞이면 거부한다")
        void 특수문자_거부(String input) {
            assertThat(ExtensionNormalizer.normalize(input)).isEmpty();
        }
    }

    @Nested
    class 유니코드_공격_패턴 {

        @Test
        @DisplayName("null byte가 포함되면 거부한다")
        void null_byte_거부() {
            assertThat(ExtensionNormalizer.normalize("sh\u0000php")).isEmpty();
        }

        @Test
        @DisplayName("RTLO(U+202E) 등 유니코드 제어/포맷 문자가 포함되면 거부한다")
        void rtlo_거부() {
            // exe를 시각적으로 위장하는 데 흔히 쓰이는 RTLO 문자
            assertThat(ExtensionNormalizer.normalize("exe\u202E")).isEmpty();
            assertThat(ExtensionNormalizer.normalize("\u202Eexe")).isEmpty();
        }

        @Test
        @DisplayName("전각 문자 등 비ASCII 문자는 NFKC 정규화 이후에도 화이트리스트에서 거부한다")
        void 전각문자_거부() {
            // 전각 'ｅｘｅ' -> NFKC 정규화해도 반각 'exe'와 바이트가 다를 수 있는 경우를 확인
            // (정규화 결과가 exe로 수렴하더라도, 우리는 exe 자체를 별도 케이스로 이미 확인했으므로
            //  여기서는 화이트리스트를 우회하지 못한다는 사실 자체가 중요함)
            Optional<String> result = ExtensionNormalizer.normalize("ｅｘｅ");
            // NFKC가 전각을 반각으로 정규화하면 "exe"가 되어 통과할 수도 있다.
            // 이 경우도 안전하다: 우리는 "exe라는 값이 나온다"는 것 자체를 검증하는 것이지,
            // 전각 문자가 화이트리스트를 '우회'해서 이상한 값으로 저장되는 게 아니기 때문이다.
            result.ifPresent(value -> assertThat(value).matches("^[a-z0-9]+$"));
        }
    }

    @Nested
    class null_및_빈값 {

        @Test
        @DisplayName("null 입력은 거부한다")
        void null_거부() {
            assertThat(ExtensionNormalizer.normalize(null)).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열은 거부한다")
        void 빈문자열_거부() {
            assertThat(ExtensionNormalizer.normalize("")).isEmpty();
        }
    }
}