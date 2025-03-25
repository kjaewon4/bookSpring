package com.book.book.service;


import com.book.book.dto.BookDetailDto;
import com.book.book.dto.BookDto;
import com.book.book.dto.TbBookStoreResponseDto;
import com.book.book.entity.TbBook;
import com.book.book.entity.TbBookKeyword;
import com.book.book.entity.TbNewsKeyword;
import com.book.book.repository.TbBookRepository;
import com.book.book.repository.TbNewsKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.data.domain.Pageable; // ✅ 올바른 Pageable (Spring Data)
import org.springframework.data.domain.Page; // ✅ Page도 함께 import

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TbBookService {
    private final TbBookRepository tbBookRepository;
    private final TbBookStoreService tbBookStoreService;
    private final TbNewsKeywordRepository tbNewsKeywordRepository;

    // 제목 검색 (페이징 포함)
    public Page<BookDto> searchBooksByTitle(String search, Pageable pageable) {
        Page<TbBook> books = tbBookRepository.findByBookTitleContainingIgnoreCase(search, pageable);
        return books.map(BookDto::new);
    }

    public Page<BookDto> getBooksByCategory(String category, Pageable pageable) {
        Page<TbBook> books = tbBookRepository.findAllByBookCategory(category, pageable);
        return books.map(BookDto::new);
    }


    // BookDetailDto 반환 메서드 (ISBN으로 조회)
    public Mono<ResponseEntity<BookDetailDto>> getBookDetailDtoByIsbn(String isbn) {
        // 1) 책 정보 가져오기
        return Mono.justOrEmpty(tbBookRepository.findByBookIsbn(isbn))
                .switchIfEmpty(Mono.error(new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다.")))
                .flatMap(tbBook -> {
                    // BookDetailDto 생성
                    BookDetailDto bookDetailDto = BookDetailDto.builder()
                            .bookIsbn(tbBook.getBookIsbn())
                            .bookTitle(tbBook.getBookTitle())
                            .bookPublisher(tbBook.getBookPublisher())
                            .bookAuthor(tbBook.getBookAuthor())
                            .bookImg(tbBook.getBookImg())
                            .bookDescription(tbBook.getBookDescription())
                            .bookCategory(tbBook.getBookCategory())
                            .build();

                    // 비동기적으로 서점 정보 가져오기 (Reactive)
                    Mono<TbBookStoreResponseDto> bookStoreMono = tbBookStoreService.fetchBookStores(isbn)
                            .doOnNext(bookStoreResponse -> System.out.println("bookStoreResponse 내용: " + bookStoreResponse));

                    // 동기적으로 반환되는 뉴스 데이터를 Mono로 감싸기
                    Mono<List<TbNewsKeyword>> newsMono = Mono.fromCallable(() -> tbNewsKeywordRepository.findAllByBooksIsbn(isbn))
                            .subscribeOn(Schedulers.boundedElastic());

                    // 두 Mono를 병합하여 DTO 생성
                    return Mono.zip(bookStoreMono, newsMono)
                            .map(tuple -> {
                                TbBookStoreResponseDto bookStoreResponse = tuple.getT1();
                                List<TbNewsKeyword> newsList = tuple.getT2();

                                if (bookStoreResponse.getItemOffStoreList() != null) {
                                    bookDetailDto.setBookStores(bookStoreResponse.getItemOffStoreList());
                                } else {
                                    System.out.println("itemOffStoreList is null!");
                                }
                                bookDetailDto.setNewsList(newsList);
                                return ResponseEntity.ok(bookDetailDto);
                            });
                });

    }

    public Page<BookDto> getBookDto(Page<TbBook> bookPage) {
        for (TbBook tb : bookPage.getContent()) {
            if (tb == null) {
                System.out.println("❗ Null TbBook 객체 발견");
            } else {
                System.out.println("도서 제목: " + tb.getBookTitle());
            }
        }

        return bookPage.map(tb -> new BookDto(
                tb.getBookIsbn(),
                tb.getBookTitle(),
                tb.getBookPublisher(),
                tb.getBookAuthor(),
                tb.getBookImg(),
                tb.getBookDescription(),
                tb.getBookCategory()
        ));
    }


    public List<BookDto> getBookDto(List<TbBook> bookList) {
        return bookList.stream()
                .map(tb -> new BookDto(
                        tb.getBookIsbn(),
                        tb.getBookTitle(),
                        tb.getBookPublisher(),
                        tb.getBookAuthor(),
                        tb.getBookImg(),
                        tb.getBookDescription(),
                        tb.getBookCategory()))
                .collect(Collectors.toList());
    }
//

    // TbBook을 조회한 후 자동으로 관련된 키워드들을 함께 가져오는 방식
    public TbBook getBookWithKeywords(String isbn) {
        TbBook tbBook = tbBookRepository.findByBookIsbn(isbn)
                .orElseThrow(() -> new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다."));
        /**
         * TbBook에서 @OneToMany 관계로 TbBookKeyword를 연결했기 때문에, tbBook.getKeywords()를 호출하면 연관된 키워드들이 자동으로 로딩됩니다.
         * 이때 Lazy Loading이 적용되어 있으므로, TbBook 객체가 실제로 조회될 때 관련된 **TbBookKeyword**들은 필요할 때 로딩됩니다.
         */
        // Lazy loading으로 keyword 가져옴
        List<TbBookKeyword> keywords = tbBook.getKeywords();  // 이 시점에서 키워드들이 Lazy Loading으로 가져와짐

        return tbBook;  // `tbBook` 객체는 이미 `keywords`를 포함하고 있음

    }

    public Page<BookDto> getBooksByIsbnList(List<String> isbnList, Pageable pageable) {
        Page<TbBook> bookPage = tbBookRepository.findByBookIsbnIn(isbnList, pageable);
        return getBookDto(bookPage);
    }

    public Page<BookDto> getAllBooks(Pageable pageable) {
        Page<TbBook> bookPage = tbBookRepository.findAll(pageable);
        return getBookDto(bookPage);
    }

    TbBook book1 = new TbBook(
            "소년이로 (편혜영 소설집)",
            "https://shopping-phinf.pstatic.net/main_3243615/32436154262.20221019142158.jpg?type=w300",
            "편혜영",
            "문학과지성사",
            "9788932035338",
            "우리는 삶이 만들어놓은 문제에 대한 답을 구할 수 있을까?\n" +
                    "\n" +
                    "장편소설 《홀》로 2017년 셜리 잭슨상을 수상하며 미국 문학 시장에서 한국 문학의 가능성을 증명해낸 바 있는 작가 편혜영의 열 번째 책이자 다섯 번째 소설집 『소년이로』. 2013년 발표한 《밤이 지나간다》 이후 6년 만에 그간의 단편소설들을 엮어 펴낸 소설집으로, 《뉴요커The New Yorker》에 게재되면서 한국 문학의 세계화를 이끌어낸 《식물 애호》와 현대문학상 수상작 《소년이로少年易老》를 담았다.\n" +
                    "\n" +
                    "흔히 소년은 늙기 쉽지만 학문을 익히는 것은 어렵다는 의미로 잘 알려진, 주자의 문집에 수록된 시 ‘소년이로학난성(少年易老學難成)’의 앞부분을 따온 것으로 보이는 표제작 《소년이로少年易老》는 자신들을 둘러싼 환경의 혼란스러움을 이해하기도 어려운 나이에, 단숨에 어른이 된 유준과 소진의 이야기를 담고 있다.\n" +
                    "\n" +
                    "어른이 된다는 것은 무엇일까. 저자는 바로 이 지점에서 독자들에게 묵직한 질문을 하나 던진다. 삶에 예상치 못한 일들이 발생해도 다 감당할 수 있겠느냐고. 대체 누구 잘못이냐고, 누구의 잘못으로 내가 이런 고통을 겪고 있는 것이냐고. 자기 자신 외에 누구도 탓하기 어려운 고통 속에서 그 불편한 진실을 감당할 수 있을지 생각하게 하는 작품들을 만나볼 수 있다.", // description (임시)
            "문학"
    );

    TbBook book2 = new TbBook(
            "82년생 김지영",
            "https://shopping-phinf.pstatic.net/main_3246707/32467074651.20231003084626.jpg?type=w300",
            "조남주",        // author (추정)
            "민음사",        // publisher (추정)
            "9788937473135", // ISBN (예시)
            "공포, 피로, 당황, 놀람, 혼란, 좌절의 연속에 대한 한국 여자의 인생 현장 보고서!\n" +
                    "\n" +
                    "문학성과 다양성, 참신성을 기치로 한국문학의 미래를 이끌어 갈 신예들의 작품을 엄선한 「오늘의 젊은 작가」의 열세 번째 작품 『82년생 김지영』. 서민들의 일상 속 비극을 사실적이면서 공감대 높은 스토리로 표현하는 데 재능을 보이는 작가 조남주는 이번 작품에서 1982년생 '김지영 씨'의 기억을 바탕으로 한 고백을 한 축으로, 고백을 뒷받침하는 각종 통계자료와 기사들을 또 다른 축으로 삼아 30대를 살고 있는 한국 여성들의 보편적인 일상을 완벽하게 재현한다.\n" +
                    "\n" +
                    "슬하에 딸을 두고 있는 서른네 살 김지영 씨가 어느 날 갑자기 이상 증세를 보인다. 시댁 식구들이 모여 있는 자리에서 친정 엄마로 빙의해 속말을 뱉어 내고, 남편의 결혼 전 애인으로 빙의해 그를 식겁하게 만들기도 한다. 이를 이상하게 여긴 남편이 김지영 씨의 정신 상담을 주선하고, 지영 씨는 정기적으로 의사를 찾아가 자신의 삶을 이야기한다. 소설은 김지영 씨의 이야기를 들은 담당 의사가 그녀의 인생을 재구성해 기록한 리포트 형식이다. 리포트에 기록된 김지영 씨의 기억은 ‘여성’이라는 젠더적 기준으로 선별된 에피소드로 구성된다.\n" +
                    "\n" +
                    "1999년 남녀차별을 금지하는 법안이 제정되고 이후 여성부가 출범함으로써 성평등을 위한 제도적 장치가 마련된 이후, 즉 제도적 차별이 사라진 시대에 보이지 않는 방식으로 존재하는 내면화된 성차별적 요소가 작동하는 방식을 보여 준다. 지나온 삶을 거슬러 올라가며 미처 못다 한 말을 찾는 이 과정은 지영 씨를 알 수 없는 증상으로부터 회복시켜 줄 수 있을까? 김지영 씨로 대변되는 ‘그녀’들의 인생 마디마디에 존재하는 성차별적 요소를 핍진하게 묘사하고 있다.",
            "한국소설"
    );

    TbBook book3 = new TbBook(
            "작별하지 않는다",
            "https://shopping-phinf.pstatic.net/main_3243636/32436366634.20231124160335.jpg?type=w300",
            "한강",          // author (추정)
            "문학동네",       // publisher (추정)
            "9788954682152", // ISBN (예시)
            "무엇을 생각하면 견딜 수 있나.\n" +
                    "가슴에 활활 일어나는 불이 없다면.\n" +
                    "기어이 돌아가 껴안을 네가 없다면.\n" +
                    "\n" +
                    "이곳에 살았던 이들로부터, 이곳에 살아 있는 이들로부터\n" +
                    "꿈처럼 스며오는 지극한 사랑의 기억\n" +
                    "\n" +
                    "2016년 『채식주의자』로 인터내셔널 부커상을 수상하고 2018년 『흰』으로 같은 상 최종 후보에 오른 한강 작가의 5년 만의 신작 장편소설 『작별하지 않는다』가 출간되었다. 2019년 겨울부터 이듬해 봄까지 계간 『문학동네』에 전반부를 연재하면서부터 큰 관심을 모았고, 그뒤 일 년여에 걸쳐 후반부를 집필하고 또 전체를 공들여 다듬는 지난한 과정을 거쳐 완성되었다. 본래 「눈 한 송이가 녹는 동안」(2015년 황순원문학상 수상작), 「작별」(2018년 김유정문학상 수상작)을 잇는 ‘눈’ 3부작의 마지막 작품으로 구상되었으나 그 자체 완결된 작품의 형태로 엮이게 된바, 한강 작가의 문학적 궤적에서 『작별하지 않는다』가 지니는 각별한 의미를 짚어볼 수 있다. 이로써 『소년이 온다』(2014), 『흰』(2016), ‘눈’ 연작(2015, 2017) 등 근작들을 통해 어둠 속에서도 한줄기 빛을 향해 나아가는 인간의 고투와 존엄을 그려온 한강 문학이 다다른 눈부신 현재를 또렷한 모습으로 확인할 수 있게 되었다. 오래지 않은 비극적 역사의 기억으로부터 길어올린, 그럼에도 인간을 끝내 인간이게 하는 간절하고 지극한 사랑의 이야기가 눈이 시리도록 선연한 이미지와 유려하고 시적인 문장에 실려 압도적인 아름다움으로 다가온다.",
            "한국소설"
    );


    public void saveBooks() {
        tbBookRepository.save(book1);
        tbBookRepository.save(book2);
        tbBookRepository.save(book3);
    }


}
