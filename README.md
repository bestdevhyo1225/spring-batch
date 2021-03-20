# Batch Application

## BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION 메타 테이블

### BATCH_JOB_INSTANCE 메타 테이블

- `Job Parameter`에 따라 생성되는 테이블

    - `Job Parameter`란 Spring Batch가 실행될 때 외부에서 받을 수 있는 파라미터
    
    - 특정 날짜를 `Job Parameter`로 넘기면, Spring Batch에서는 해당 날짜 데이터로 조회, 가공, 입력등의 작업을 할 수 있다.
    
- 같은 Batch Job이라도 `Job Parameter`가 다르면, `BATCH_JOB_INSTANCE` 테이블에 기록된다.

- `Job Parameter`가 같다면, 기록되지 않는다. (`JobInstanceAlreadyCompleteException`이 발생한다.)

- 동일한 `Job Parameter`로 성공한 기록이 있을때만 재수행이 안되고, 실패한 기록이 있다면 재수행이 된다. (즉, 실패한 기록이 있는 상태에서 다시 실행하고, 성공 했을때 `JobInstanceAlreadyCompleteException`가 발생하지 않는다.)

### BATCH_JOB_EXECUTION 메타 테이블

- `BATCH_JOB_INSTANCE` 테이블과 `부모-자식` 관계이다.

- `BATCH_JOB_INSTANCE` 테이블이 `부모` 이고, `BATCH_JOB_EXECUTION` 테이블이 `자식` 이라고 생각하면 된다.

- `BATCH_JOB_EXECUTION` 테이블은 자신의 부모인 `BATCH_JOB_INSTANCE`의 `성공/실패` 했던 모든 내역을 가지고 있다.

## StepScope, JobScope

`Spring`에서 `Bean`의 기본 스코프는 `Singleton`이다. 그런데 `Spring Batch`에서 특별하게 사용되는 `StepScope`와 `JobScope`가 있다.

### 가장 중요한 특징

- Spring 컨테이너를 통해 지정된 `Step, Job 실행 시점에 해당 컴포넌트를 Spring Bean`으로 생성한다. 

- 즉, `Bean의 생성 시점을 지정된 Scope가 실행되는 시점`으로 지연시킨다.

- `Spring의 Request Scope` 처럼 `Step, Job이 실행되고 끝날때 생성 및 삭제가 이루어진다.`

### Bean 생성 시점을 Application 실행 시점이 아닌, Step 혹은 Job의 실행 시점으로 지연 시키면서 얻는 장점?

1. `Job Parameter`의 `Late Binding`이 가능하다.

    - `Job Parameter`가 `Step Context` 또는 `JobExecutionContext` 레벨에서 할당 시킬 수 있다.
  
    - Application이 실행되는 시점이 아니더라도 비즈니스 로직 단계(Controller, Service)에서 `Job Parmeter`를 할당 시킬 수 있다.
  
2. 동일한 컴포넌트를 `병렬 혹은 동시에 사용할 때 유리`하다.

    - Step 안에 Tasklet이 있고, Tasklet은 멤버 변수와 멤버 변수를 변경하는 로직이 있다고 가정
    
    - `@StepScope` 없이 Step을 병렬적으로 실행시키게 되면, 서로 다른 `Step`에서 하나의 `Tasklet`을 두고 마구잡이로 상태를 변경하려고 할 것이다.
  
    - `@StepScope`가 있다면, 각각의 Step에서 별도의 `Tasklet을 생성하고, 관리하기 때문에 서로의 상태를 침범할 일이 없다.`
  
## Job Parameter

### 중요한 특징 

- `Job Parameter`는 Step, Tasklet, Reader등 Batch 컴포넌트 Bean 생성의 생성 시점에만 호출할 수 있다.

- 정확히는 `Scope Bean`을 생성할때만 가능하다. 즉, `@StepScope`, `@JobScope`와 같은 `Bean을 생성할때만 Job Parameter가 생성되기 때문에 사용할 수 있다.`
 
### Job Parameter vs 시스템 변수 (Job Parameter를 써야만 하는 이유)

- `시스템 변수`를 사용하게 되면 `Spring Batch의 Job Parameter 관련 기능을 쓰지 못하게 된다.`

- `Spring Batch`에서는 동일한 `Job Parameter`에 대해서 `같은 Job을 두 번 실행하지 않는다.` 그런데 `시스템 변수`를 사용하게 되면, 이 기능이 전혀 작동되지 않는 문제가 있다.

- 추가로 `Spring Batch`에서 자동으로 관리해주는 `Parameter 관련 메타 테이블`이 관리되지 않는 문제도 가지고 있다.

- `시스템 변수`를 사용하면 Command Line이 아닌 다른 방법으로 Job을 실행하기 어렵다. 만약 실행해야 한다면, `전역상태(시스템, 환경 변수)를 동적으로 계속해서 변경시킬 수 있도록` Spring Batch를 구성해야 한다.

- `Job Parameter`를 사용하지 못하면, `Late Binding을 못하게 되는` 의미이다.

- 외부에서 넘겨주는 파라미터에 따라 Batch가 다르게 동작해야 한다면, 이를 `시스템 변수`로 풀어내는 것은 너무나 어렵다.

## 참고

- [3. Spring Batch 가이드 - 메타 테이블 엿보기](https://jojoldu.tistory.com/326?category=902551)

- [5. Spring Batch 가이드 - Spring Batch Scope & Job Parameter](https://jojoldu.tistory.com/330?category=902551)
