# Passion Daily

매일 새로운 영감을 주는 명언 앱 - 고전부터 현대까지의 명언으로 매일 새로운 영감을 제공하는 애플리케이션

## 📌 프로젝트 개요

### 한 줄 요약
고전부터 현대까지의 명언으로 매일 새로운 영감을 제공하는 애플리케이션

### 목적

"'그 명언 어디선가 봤던 것 같은데?' 그리고 '왜 명언 앱들은 항상 옛날 사람들 말만 모아놨을까?'"

이 두 가지 고민에서 출발했습니다. 현대인들은 다양한 미디어에서 좋은 명언을 접해도 어느순간 기억에서 사라지고, 기존 명언 앱들은 젊은 세대의 니즈를 크게 충족시키지 못한다는 사실을 인식하였습니다. 따라서 사용자가 마음에 드는 문구를 쉽게 저장하고 공유할 수 있고, 다양한 시대의 명언을 제공하는 명언 앱을 구상하고 개발하게 되었습니다.

### 기술 스택

- **사용 언어**: Kotlin
- **사용 기술**: Jetpack Compose, Room, Firestore, Jetpack Navigation, Hilt, Compose UI Test, JUnit4
- **개발 기간**: 2024.11-2025.03
- **역할**: 1인 개발

## 📱 스크린샷

<p align="center">
  <img src="https://github.com/YoonseolLee/Passion-Daily-Photos/blob/main/KakaoTalk_20250306_165551598.jpg?raw=true" width="200"/>
  <img src="https://github.com/YoonseolLee/Passion-Daily-Photos/blob/main/KakaoTalk_20250306_165551598_01.jpg?raw=true" width="200"/>
</p>

## 📊 데이터베이스 구조

### Local DB (Room)
![image](https://github.com/user-attachments/assets/64621b07-3204-4e3c-8a1c-b9faced742d9)


### Remote DB (Firestore)
![image](https://github.com/user-attachments/assets/60522a32-d63a-445c-837b-1349c12cdc0e)


> **💡 Firestore에서 별도의 quotes와 categories 컬렉션 구조 대신 단일 컬렉션 구조를 선택했습니다.**
> 
> **이유**: 쿼리 성능 최적화 때문입니다. 카테고리별 명언 조회가 앱에서 가장 빈번한 작업이므로, 2개의 컬렉션에 대해 쿼리를 생성하는 것 보다는, 단일 쿼리만으로 1개의 컬렉션에서 필요한 데이터를 빠르게 가져올 수 있다고 생각했습니다.

## 🏗️ 아키텍처

### 1. MVVM 패턴
View와 Model이 서로 전혀 알지 못하도록 하여 독립성을 높이고, View 로직과 비지니스 로직을 분리하여 생산성을 높였습니다.

### 2. 클린 아키텍처
안드로이드 공식 문서를 기반으로 클린 아키텍처를 도입하였습니다. UI 레이어, 도메인 레이어, 데이터 레이어로 관심사를 분리하였고, StateHolder, Coroutines, Flow를 적극적으로 사용하여 유지보수와 테스트가 용이한 구조로 설계하였습니다.

### 3. SOLID 원칙
객체지향 설계 원칙을 적용하여 코드의 확장성과 유지보수성을 향상시켰습니다.

### 4. Hilt를 사용한 의존성 주입
Hilt를 사용해 의존성 주입을 간편하게 처리하고, 결합도를 낮추며 모듈 내 응집도를 높였습니다.

## 🧩 SOLID 원칙을 적용한 프로젝트 구조

### 1. 단일 책임 원칙 (Single Responsibility Principle)

```
com.example.passiondaily/
├── quote/
│   ├── data/ (데이터 관리 책임)
│   │   ├── repository/
│   │   └── dao/
│   ├── domain/ (비즈니스 로직 책임)
│   │   └── usecase/
│   └── presentation/ (UI 표시 책임)
│       ├── screen/
│       └── viewmodel/
```

각 클래스와 모듈은 하나의 책임만을 갖습니다. 예를 들어, QuoteDao는 데이터 접근만, QuoteViewModel은 UI 상태 관리만 담당합니다.

### 2. 개방-폐쇄 원칙 (Open-Closed Principle)

```
├── manager/
│   ├── QuoteLoadingManager (인터페이스)
│   └── QuoteLoadingManagerImpl (구현체)
```

QuoteLoadingManager 인터페이스를 통해 확장에는 열려있고, 수정에는 닫혀있는 구조입니다.

### 3. 리스코프 치환 원칙 (Liskov Substitution Principle)

```
├── favorites/
│   ├── base/
│   │   ├── FavoritesViewModelActions
│   ├── action/
│   │   ├── FavoritesLoadingActions 
│   │   ├── FavoritesModificationActions 
│   │   └── FavoritesNavigationActions 
```

부모 클래스(인터페이스)를 자식 클래스(구현체)로 대체해도 정상적으로 동작해야 합니다. FavoritesViewModelActions라는 기본 인터페이스를 여러 구현체가 구현하고 있으며, 이들은 서로 대체 가능합니다.

### 4. 인터페이스 분리 원칙 (Interface Segregation Principle)

```
├── action/
│   ├── QuoteLoadingActions (로딩 기능만 포함)
│   ├── QuoteNavigationActions (탐색 기능만 포함)
│   └── QuoteSharingActions (공유 기능만 포함)
```

기능별로 인터페이스를 분리하였습니다.

### 5. 의존성 역전 원칙 (Dependency Inversion Principle)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindLocalFavoriteRepository(repository: LocalFavoriteRepositoryImpl): 
    LocalFavoriteRepository
}
```

고수준 모듈이 저수준 모듈에 직접 의존하지 않고, 추상화(인터페이스)에 의존합니다.

## 🔍 주요 기능

### 1. 명언을 불러오는 기능

```kotlin
override fun loadQuotes(category: QuoteCategory) {
    viewModelScope.launch {
        try {
            quoteStateHolder.updateIsQuoteLoading(true)

            val result = quoteLoadingManager.fetchQuotesByCategory(
                category = category,
                pageSize = PAGE_SIZE,
                lastLoadedQuote = lastLoadedQuote
            )

            if (result.quotes.isEmpty()) {
                quoteLoadingManager.setHasQuoteReachedEndTrue()
                return@launch
            }

            if (quotes.value.isEmpty()) {
                lastLoadedQuote = result.lastDocument
                quoteLoadingManager.addQuotesToState(result.quotes, true)
            } else {
                lastLoadedQuote = result.lastDocument
                quoteLoadingManager.addQuotesToState(result.quotes, false)
            }
        } catch (e: Exception) {
            handleError(e)
        } finally {
            quoteStateHolder.updateIsQuoteLoading(false)
        }
    }
}
```

#### 1.1 코루틴을 활용한 비동기 처리

> **💡 Firestore에서 명언을 불러올 때 Dispatchers.IO를 사용하였습니다.**
> 
> **이유**: Dispatchers.IO는 Firestore 같은 I/O 작업에 최적화된 스레드 풀을 제공하기 때문입니다.

#### 1.2 오류 처리 및 재시도 메커니즘

> **💡 최대 3회까지 자동 재시도 및 10초 타임아웃을 적용하였습니다.**
> 
> **이유**: 과도한 서버 부하를 방지하고, 모바일 환경에서 일반적인 데이터 요청은 1-3초 내로 완료되기 때문에 10초가 초과되면 네트워크 문제로 간주하기 위함입니다.

#### 1.3 페이지네이션 구현

> **💡 페이지 사이즈를 10으로 설정하였습니다.**
> 
> **이유**: 전체 명언 컬렉션의 평균 크기인 약 20개의 절반으로 설정했습니다. 명언 앱의 특성상 사용자가 쇼츠 콘텐츠처럼 연속적으로 소비하는 패턴을 고려하여 충분한 양을 미리 로드하도록 설계했습니다.

### 2. 즐겨찾기에 추가하는 기능

```kotlin
class SaveFavoritesToLocalUseCase @Inject constructor(
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository
) {

    suspend fun saveToLocalDatabase(
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    ) {
        ensureCategoryExists(selectedCategory)
        ensureQuoteExists(selectedCategory, currentQuote)
        saveFavorite(currentQuote.id, selectedCategory)
    }

    private suspend fun ensureCategoryExists(category: QuoteCategory) {
        if (!localQuoteCategoryRepository.isCategoryExists(category.ordinal)) {
            val categoryEntity = QuoteCategoryEntity(
                categoryId = category.ordinal,
                categoryName = category.getLowercaseCategoryId()
            )
            localQuoteCategoryRepository.insertCategory(categoryEntity)
        }
    }

    private suspend fun ensureQuoteExists(
        category: QuoteCategory,
        quote: Quote
    ) {
        if (!localQuoteRepository.isQuoteExistsInCategory(quote.id, category.ordinal)) {
            val quoteEntity = QuoteEntity(
                quoteId = quote.id,
                text = quote.text,
                person = quote.person,
                imageUrl = quote.imageUrl,
                categoryId = category.ordinal
            )
            localQuoteRepository.insertQuote(quoteEntity)
        }
    }

    private suspend fun saveFavorite(
        quoteId: String,
        category: QuoteCategory
    ) {
        val favoriteEntity = FavoriteEntity(
            quoteId = quoteId,
            categoryId = category.ordinal
        )
        localFavoriteRepository.insertFavorite(favoriteEntity)
    }
}
```

#### 2.1 즐겨찾기 추가 전 사전 검증

> **💡 즐겨찾기 저장 전 카테고리와 명언의 존재 여부를 확인하도록 구현했습니다.**
> 
> **이유**: DB의 참조 무결성을 보장하기 위함입니다. ensureCategoryExists와 ensureQuoteExists 함수를 통해 즐겨찾기 추가 이전에 사전 검증을 수행합니다.

#### 2.2 Room DAO에서 Flow 반환

> **💡 Room DAO에서 Flow를 반환하는 패턴을 적용했습니다.**
> 
> **이유**: 데이터베이스 변경사항을 UI에 자동 반영하면서도 메모리 효율성을 높이기 위함입니다. Flow의 cold stream 특성을 통해 필요 시점에만 데이터를 처리할 수 있도록 하기 위함입니다.

### 3. 즐겨찾기를 가져오는 기능

```kotlin
override fun loadFavorites() {
    favoritesJob?.cancel()
    favoritesJob = viewModelScope.launch {
        favoritesLoadingManager.updateIsFavoriteLoading(true)
        try {
            favoritesLoadingManager.getAllFavorites()
                .catch { e ->
                    favoritesLoadingManager.updateIsFavoriteLoading(false)
                    favoritesStateHolder.updateIsFavoriteLoading(false)
                    throw e
                }
                .collect { favorites ->
                    handleFavoritesUpdate(favorites)
                    favoritesLoadingManager.updateIsFavoriteLoading(false)
                }
        } catch (e: Exception) {
            favoritesLoadingManager.updateIsFavoriteLoading(false)
            handleError(e)
        }
    }
}

private fun handleFavoritesUpdate(favorites: List<QuoteEntity>) {
    favoritesLoadingManager.updateFavoriteQuotes(favorites)
    if (_currentQuoteIndex.value >= favorites.size) {
        savedStateHandle[KEY_FAVORITE_INDEX] = 0
    }
}
```

#### 3.1 중복 요청 방지를 위한 코루틴 Job 관리

> **💡 즐겨찾기 로딩 시 이전 작업을 취소하도록 구현했습니다.**
> 
> **이유**: 만약 사용자가 즐겨찾기 화면을 빠르게 여러 번 전환하거나, 목록을 자주 새로고침한다면 여러 개의 데이터 로딩 작업이 동시에 시작될 가능성이 존재합니다. loadFavorites() 호출 시 FavoritesJob?.cancel()로 이전 데이터 로딩을 취소함으로써 loadFavorites()가 중복되게 호출되는 상황을 방지하기 위함입니다.

#### 3.2 SavedStateHandle을 활용한 상태 보존

> **💡 즐겨찾기 목록의 인덱스를 SavedStateHandle로 관리하였습니다.**
> 
> **이유**: 화면 회전이나 프로세스 재생성 시에도 사용자가 마지막으로 본 명언 인덱스를 유지하기 위함입니다. 또한 예외 상황으로 인해 목록 크기가 변경되면, 인덱스를 자동으로 조정하여 항상 유효한 데이터를 표시하도록 하였습니다.

## 🚀 성능 최적화

### Flow 구독 최적화

```kotlin
// 최적화 전
override val currentQuote: StateFlow<Quote?> =
    combine(quotes, _currentQuoteIndex) { quotes, index ->
        quotes.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

// 최적화 후
override val currentQuote: StateFlow<Quote?> =
    combine(quotes, _currentQuoteIndex) { quotes, index ->
        quotes.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
```

**문제점**:
QuoteViewModel에서 `SharingStarted.Lazily` 전략을 사용하여 화면을 벗어나도 Flow가 계속 활성 상태로 유지되었습니다.

**원인**:
`SharingStarted.Lazily`는 첫 번째 구독자가 나타난 후 계속 활성 상태를 유지하므로 화면을 벗어나도 불필요한 리소스를 소비하기 때문입니다.

**해결방안**:
`SharingStarted.WhileSubscribed(5000)`으로 변경하여 마지막 구독자가 사라진 후 5초 뒤에 자동으로 Flow를 취소하도록 하였습니다. 5초의 타임아웃을 설정한 이유는 일반적인 명언 앱의 사용자가 5초 안에 돌아오지 않으면 다른 활동으로 전환했을 가능성이 크기 때문입니다.

### 애니메이션 최적화

```kotlin
// 최적화 전
AnimatedContent(
    targetState = currentQuote,
    transitionSpec = {
        (slideIntoContainer(slideDirection, animationSpec = ContentAnimationSpec) +
                fadeIn(animationSpec = FadeAnimationSpec)).togetherWith(
            slideOutOfContainer(slideDirection, animationSpec = ContentAnimationSpec) +
                    fadeOut(animationSpec = FadeAnimationSpec)
        )
    }
) { quote ->
    quote?.let {
        QuoteAndPerson(
            quote = it.text,
            author = it.person
        )
    }
}

// 최적화 후
val currentQuoteId = remember {
    derivedStateOf {
        currentQuote?.id ?: ""
    }
}.value

AnimatedContent(
    targetState = currentQuoteId,
    transitionSpec = {
        (slideIntoContainer(
            slideDirection,
            animationSpec = ContentAnimationSpec
        ) +
                fadeIn(animationSpec = FadeAnimationSpec)).togetherWith(
            slideOutOfContainer(
                slideDirection,
                animationSpec = ContentAnimationSpec
            ) +
                    fadeOut(animationSpec = FadeAnimationSpec)
        )
    }
) { quoteId ->
    val displayedQuote = remember(quoteId, quotes) {
        quotes.find { it.id == quoteId } ?: currentQuote
    }

    key(displayedQuote?.id) {
        displayedQuote?.let {
            QuoteAndPerson(
                quote = it.text,
                author = it.person
            )
        }
    }
}
```

**문제점**:
`AnimatedContent`가 전체 `currentQuote` 객체를 타겟으로 사용하여 불필요한 리컴포지션과 애니메이션이 발생하여 성능에 악영향을 줍니다.

**원인**: 
Quote 객체를 targetState로 설정하면, 객체의 어떤 속성이 변경되더라도 전체 컴포지션이 다시 발생하고 애니메이션이 트리거됩니다. 객체 내용이 동일하고 참조만 변경된 경우에도 불필요한 애니메이션을 발생시킵니다.

**해결방안**:
1. derivedStateOf를 사용하여 currentQuote에서 ID만 추출하도록 하였습니다. quote 객체의 다른 속성이 변경되어도 ID가 동일하면 애니메이션이 트리거되지 않습니다.
2. targetState로 quoteId를 사용하여 필요한 경우에만 애니메이션이 발생하도록 하였습니다.
3. `remember`를 사용해 quoteId와 quotes가 변경될 때만 displayedQuote를 다시 계산하도록 하였습니다.
4. key를 사용하여 quoteId가 변경될 때에만 내부 컴포넌트를 다시 구성하도록 하였습니다.

### 성능 최적화 결과

애플리케이션 성능 최적화 작업의 결과, **사용자 CPU 시간이 약 2.4% 감소**하였으며 **애니메이션 FPS가 최대 9.3% 향상**되었습니다.

#### 1. CPU 사용량 비교 테스트

| 항목 | 개선 전 | 개선 후 | 변화율 |
| --- | --- | --- | --- |
| 사용자 CPU 시간 (시간당) | 14분 44초 | 14분 22초 | -2.4% |
| 실제 측정 사용자 CPU 시간 | 48.957초 | 47.719초 | -2.5% |

#### 2. FPS 테스트 비교 테스트

| 지표 | 수정 전 | 수정 후 | 개선 결과 |
| --- | --- | --- | --- |
| 기본 FPS 안정성 | 59.73-60.13 | 59.90-60.01 | **개선됨** (더 일관된 값) |
| 최대 FPS | 101.87 | 111.35 | **개선됨** (+9.3%) |
| 애니메이션 중 평균 FPS | 약 73.14 | 약 78.64 | **개선됨** (+7.5%) |
| 유휴 상태 FPS 변동 | ±0.15 | ±0.05 | **개선됨** (3배 더 안정적) |

## 🧪 테스트

### UI 테스트

```kotlin
@RunWith(AndroidJUnit4::class)
class CategoryScreenTest {
    private val mainCoroutineRule = MainCoroutineRule()
    private val composeTestRule = createComposeRule()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(mainCoroutineRule)
        .around(composeTestRule)

    private val quoteViewModel = mockk<QuoteViewModel>(relaxed = true)
    private val quoteStateHolder = mockk<QuoteStateHolder>(relaxed = true)

    @Before
    fun setup() {
        every { quoteStateHolder.categories } returns MutableStateFlow(
            QuoteCategory.values().map { it.koreanName }
        )
        every { quoteStateHolder.selectedQuoteCategory } returns MutableStateFlow(QuoteCategory.EFFORT)
    }

    @Test
    fun 카테고리_화면이_올바르게_표시된다() = mainCoroutineRule.runTest {
        // When
        composeTestRule.setContent {
            CategoryScreen(
                quoteViewModel = quoteViewModel,
                quoteStateHolder = quoteStateHolder,
                onNavigateToQuote = {},
                onBack = {}
            )
        }

        // Then
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("카테고리")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNode(hasText("오늘은 어떤 주제의 명언을 만나볼까요?"))
            .assertExists()
            .assertIsDisplayed()
    }
}
```

Compose UI 테스트 프레임워크를 사용하여 UI 테스트 코드를 작성했습니다. `createComposeRule()`을 통해 Compose 컴포넌트를 독립적으로 테스트하고, MockK를 사용하여 ViewModel과 StateHolder를 모킹하여 테스트 코드를 작성했습니다.

### 단위 테스트

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class IncrementShareCountUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var useCase: IncrementShareCountUseCase
    private lateinit var remoteQuoteRepository: RemoteQuoteRepository

    @Before
    fun setup() {
        remoteQuoteRepository = mockk {
            coEvery { incrementShareCount(any(), any()) } returns Unit
        }
        useCase = IncrementShareCountUseCase(remoteQuoteRepository)
    }

    @Test
    fun `공유 횟수를 증가시킬 때 repository를 호출한다`() = mainCoroutineRule.runTest {
        // given
        val quoteId = "test_quote_id"
        val category = QuoteCategory.LOVE

        // when
        useCase.incrementShareCount(quoteId, category)
        advanceUntilIdle()

        // then
        coVerify { remoteQuoteRepository.incrementShareCount(quoteId, category) }
    }
}
```

비즈니스 로직을 검증하기 위해 JUnit과 MockK를 사용한 단위 테스트를 구현했습니다.

### 통합 테스트

다양한 하드웨어 환경에서의 앱 성능과 안정성을 검증하기 위해 세 그룹으로 나누어 통합 테스트를 진행했습니다.

- **그룹 1 (고사양)**: AVD를 통해 최신 하드웨어 스펙에서의 동작 검증
- **그룹 2 (중사양)**: 실제 물리적 기기를 사용하여 실제 사용 환경에서의 사용자 경험 검증
- **그룹 3 (저사양)**: 제한된 리소스 환경에서의 성능과 안정성 검증을 위한 AVD 테스트

실제 테스트 결과는 [Passion-Daily-통합테스트](https://docs.google.com/spreadsheets/d/17egLQB4Slzd_o4PIL5EtBCU0fKos96ydCCDpNHdRMiw/edit?usp=sharing)에서 확인하실 수 있습니다.
