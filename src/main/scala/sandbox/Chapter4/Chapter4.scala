package sandbox.Chapter4

import cats.Monoid


object Chapter4 {
  /* 4. Monads
  * モナドは、Scalaで最も一般的な抽象化の1つです。
  * 多くのScalaプログラマーは、名前でモナドを知らなくても、すぐにモナドに直感的に慣れます。
  * 非公式には、モナドはコンストラクターとflatMapメソッドを持つものです。
  * 前の章で見たすべてのファンクターも、Option、List、Futureなどのモナドです。
  * モナドをサポートするための特別な構文もあります：内包表記用。
  * ただし、概念が広く普及しているにもかかわらず、Scala標準ライブラリには、
  * 「flatMapできるもの」を網羅する具体的なタイプがありません。 この型クラスは、Catsがもたらすメリットの1つです。
  * この章では、モナドについて深く掘り下げます。
  * いくつかの例を挙げて、彼らをやる気にさせることから始めます。
  * Catsでの正式な定義と実装に進みます。 最後に、見たことのない興味深いモナドをいくつか紹介し、その使用例を紹介します。
  *
  * 4.1 What is a Monad?
  * これは、1000のブログ投稿で提起された質問であり、猫、メキシコ料理、有毒廃棄物でいっぱいの宇宙服、
  * エンドファンクターのカテゴリーのモノイド（それが意味するものは何でも）などの多様な概念を含む説明と類推があります。
  * 非常に簡単に述べることで、モナドを一度に説明するという問題を解決します。
  *
  * モナドは、計算を順序付けるためのメカニズムです。
  *
  * 簡単でした！ 問題は解決しましたよね？
  * しかし、繰り返しになりますが、前の章では、ファンクターはまったく同じことの制御メカニズムであると述べました。
  * わかりました、多分私達はもう少し議論が必要です…
  * セクション3.1で、ファンクターを使用すると、複雑さを無視して計算をシーケンスできると述べました。
  * ただし、ファンクターは、この合併症がシーケンスの開始時に1回だけ発生することを許可するという点で制限されています。
  * シーケンスの各ステップでのさらなる複雑さは考慮されていません。
  * これがモナドの出番です。モナドのflatMapメソッドを使用すると、中間の複雑さを考慮して、次に何が起こるかを指定できます。
  * OptionのflatMapメソッドは、中間オプションを考慮に入れます。
  * ListのflatMapメソッドは、中間リストを処理します。 等々。
  * いずれの場合も、flatMapに渡される関数は、計算のアプリケーション固有の部分を指定し、flatMap自体が複雑さを処理して、
  * 再びflatMapを実行できるようにします。 いくつかの例を見て、物事を理解しましょう。
  *
  * Options
  * オプションを使用すると、値を返す場合と返さない場合がある計算をシーケンス処理できます。 ここではいくつかの例を示します。
  *
  * */

  def parseInt(str: String): Option[Int] =
    scala.util.Try(str.toInt).toOption

  def divide(a: Int, b: Int): Option[Int] =
    if (b == 0) None else Some(a / b)

  /*
  * これらの各メソッドは、Noneを返すことで「失敗」する可能性があります。
  * flatMapメソッドを使用すると、操作をシーケンスするときにこれを無視できます。
  * */

  def stringDivideBy(aStr: String, bStr: String): Option[Int] =
    parseInt(aStr).flatMap { aNum =>
      parseInt(bStr).flatMap { bNum =>
        divide(aNum, bNum)
      }
    }

  /*
  *
    セマンティクスは次のとおりです。
    parseIntを最初に呼び出すと、NoneまたはSomeが返されます。
    Someを返す場合、flatMapメソッドは関数を呼び出し、整数aNumを渡します。
    parseIntへの2番目の呼び出しは、NoneまたはSomeを返します。
    Someを返す場合、flatMapメソッドは関数を呼び出してbNumを渡します。
    除算を呼び出すと、NoneまたはSomeが返されます。これが結果です。
    各ステップで、flatMapは関数を呼び出すかどうかを選択し、関数はシーケンス内の次の計算を生成します。 これを図8に示します。
  *
  * 計算の結果はOptionであり、flatMapを再度呼び出すことができるため、シーケンスが続行されます。
  * これにより、私たちが知っていて気に入っているフェイルファストエラー処理動作が発生します。
  * どのステップでもNoneを使用すると、全体的にNoneになります。
  * */
  stringDivideBy("6", "2")
  // res0: Option[Int] = Some(3)
  stringDivideBy("6", "0")
  // res1: Option[Int] = None
  stringDivideBy("6", "foo")
  // res2: Option[Int] = None
  stringDivideBy("bar", "2")
  // res3: Option[Int] = None

  /* すべてのモナドはファンクターでもあるため（証明については以下を参照）、flatMapとmap to sequence計算の両方に依存して、
  新しいモナドを導入する場合と導入しない場合があります。
  さらに、flatMapとmapの両方がある場合、シーケンス動作を明確にするための理解に使用できます。
  * */

  def stringDivideByFor(aStr: String, bStr: String): Option[Int] =
    for {
      aNum <- parseInt(aStr)
      bNum <- parseInt(bStr)
      ans <- divide(aNum, bNum)
    } yield ans


  /* Lists
  * FlatMapを新進のScala開発者として最初に見つけたとき、私たちはそれをリストを反復するためのパターンと考える傾向があります。
  * これは、for内包表記の構文によって強化されています。これは、forループの命令型に非常によく似ています。
  */

  for {
    x <- (1 to 3).toList
    y <- (4 to 5).toList
  } yield (x, y)

  /*
  * ただし、リストのモナド動作を強調する、適用できる別のメンタルモデルがあります。
  * リストを中間結果のセットと考えると、flatMapは順列と組み合わせを計算する構造になります。
  * たとえば、上記の理解のために、xの3つの可能な値とyの2つの可能な値があります。
  * これは、（x、y）の6つの可能な値があることを意味します。
  * flatMapは、操作のシーケンスを示すコードからこれらの組み合わせを生成しています。
  * - get x
    - get y
    - create a tuple (x, y)
  *
  * Futures
  * Futureは、非同期である可能性を心配せずに計算をシーケンスするモナドです。
  * */

  //  import scala.concurrent.Future
  //  import scala.concurrent.ExecutionContext.Implicits.global
  //
  //  def doSomethingLongRunning: Future[Int] = ???
  //  def doSomethingElseLongRunning: Future[Int] = ???
  //
  //  def doSomethingVeryLongRunning: Future[Int] =
  //    for {
  //      result1 <- doSomethingLongRunning
  //      result2 <- doSomethingElseLongRunning
  //    } yield result1 + result2

  /*
  * 繰り返しになりますが、各ステップで実行するコードを指定すると、
  * flatMapは、スレッドプールとスケジューラーの恐ろしい根本的な複雑さをすべて処理します。
  * Futureを多用している場合は、上記のコードが各操作を順番に実行していることがわかります。
  * これは、理解のために展開してflatMapへのネストされた呼び出しを表示するとより明確になります。
  * */

  //  def doSomethingVeryLongRunning: Future[Int] =
  //    doSomethingLongRunning.flatMap { result1 =>
  //      doSomethingElseLongRunning.map { result2 =>
  //        result1 + result2
  //      }
  //    }

  /*
  * シーケンス内の各Futureは、前のFutureから結果を受け取る関数によって作成されます。
  * つまり、計算の各ステップは、前のステップが終了した後でのみ開始できます。
  * これは、タイプA => Future [B]の関数パラメーターを示す図9のflatMapのタイプチャートから生まれました。
  *
  * もちろん、私たちはfuturesを並行して実行することができますが、それは別の話であり、別の機会に語られるでしょう。
  * モナドはすべてシーケンスに関するものです。
  *
  * 4.1.1 Definition of a Monad
  * 上記ではflatMapについてのみ説明しましたが、モナドの動作は2つの操作で正式にキャプチャされます。
  * - pure, of type A => F[A];
    - flatMap, of type (F[A], A => F[B]) => F[B].
  * コンストラクターに対する純粋な抽象化。
  * プレーンな値から新しいモナドコンテキストを作成する方法を提供します。
  * flatMapは、すでに説明したシーケンス手順を提供し、コンテキストから値を抽出して、シーケンス内の次のコンテキストを生成します。
  * CatsのMonad型クラスの簡略版は次のとおりです。
  * */

  //  trait Monad[F[_]] {
  //    def pure[A](value: A): F[A]
  //
  //    def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]
  //  }

  /*
  * モナド法
  * pureおよびflatMapは、意図しないグリッチや副作用なしに操作を自由にシーケンスできるようにする一連の法則に従う必要があります。
  * 左の単位元：pureを呼び出し、funcを使用して結果を変換することは、funcを呼び出すことと同じです。
  *
  * pure(a).flatMap(func) == func(a)
  *
  * 正しいアイデンティティ：pureをflatMapに渡すことは、何もしないことと同じです。
  *
  * m.flatMap(pure) == m
  *
  * 結合性：2つの関数fとgのflatMappingは、fのflatMappingとgのflatMappingと同じです。
  *
  * m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
  *
  * 4.1.2 exercise ：Func-yの取得
  * すべてのモナドはfunctorでもあります。 既存のメソッドflatMapとpureを使用して、すべてのモナドに対して同じ方法でマップを定義できます。
  * Try defining map yourself now.
  * */

  //  trait Monad[F[_]] {
  //    def pure[A](a: A): F[A]
  //
  //    def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]
  //
  //    def map[A, B](value: F[A])(func: A => B): F[B] =
  //      flatMap(value)(a => pure(func(a)))
  //  }

  /* 4.2 Monads in Cats
  * モナドに標準的な猫の処理(treatment: トリートメント！)を施す時が来ました。 いつものように、型クラス、インスタンス、構文を見ていきます。
  *
  * 4.2.1 The Monad Type Class
  * モナド型クラスはcats.Monadです。 Monadは、他の2つの型クラスを拡張します。
  * flatMapメソッドを提供するFlatMapと、pureを提供するApplicativeです。
  * ApplicativeはFunctorも拡張します。これにより、上記の演習で見たように、すべてのMonadにマップメソッドが提供されます。
  * Applicativeについては第6章で説明します。
  * 以下は、pureとflatMapを使用し、直接マップする例です。
  * */

  //  import cats.Monad
  //  import cats.instances.option._ // for Monad
  //  import cats.instances.list._ // for Monad
  //
  //  val opt1 = Monad[Option].pure(3)
  //  // opt1: Option[Int] = Some(3)
  //  val opt2 = Monad[Option].flatMap(opt1)(a => Some(a + 2))
  //  // opt2: Option[Int] = Some(5)
  //  val opt3 = Monad[Option].map(opt2)(a => 100 * a)
  //  // opt3: Option[Int] = Some(500)
  //
  //  val list1 = Monad[List].pure(3)
  //  // list1: List[Int] = List(3)
  //  val list2 = Monad[List].
  //    flatMap(List(1, 2, 3))(a => List(a, a * 10))
  //  // list2: List[Int] = List(1, 10, 2, 20, 3, 30)
  //  val list3 = Monad[List].map(list2)(a => a + 123)
  //  // list3: List[Int] = List(124, 133, 125, 143, 126, 153)

  /* Monadは、Functorのすべてのメソッドを含む、他の多くのメソッドを提供します。 詳細については、scaladocを参照してください。
  * 4.2.2 Default Instances
  * Catsは、cats.instancesを介して、標準ライブラリ（Option、List、Vectorなど）内のすべてのモナドのインスタンスを提供します。
  * */

  import cats.Monad
  import cats.instances.option._ // for Monad

  Monad[Option].flatMap(Option(1))(a => Option(a * 2))
  // res0: Option[Int] = Some(2)

  import cats.instances.list._ // for Monad

  Monad[List].flatMap(List(1, 2, 3))(a => List(a, a * 10))
  // res1: List[Int] = List(1, 10, 2, 20, 3, 30)

  import cats.instances.vector._ // for Monad

  Monad[Vector].flatMap(Vector(1, 2, 3))(a => Vector(a, a * 10))

  // res2: Vector[Int] = Vector(1, 10, 2, 20, 3, 30)

  /*
  * 猫は未来のためのモナドも提供します。
  * Futureクラス自体のメソッドとは異なり、モナドのpureメソッドとflatMapメソッドは、
  * 暗黙のExecutionContextパラメーターを受け入れることができません（パラメーターはモナドトレイトの定義の一部ではないため）。
  * これを回避するために、Catsでは、Monad for Futureを呼び出すときに、スコープ内にExecutionContextが必要です。
  * */
  //  import cats.instances.future._ // for Monad
  //  import scala.concurrent._
  //  import scala.concurrent.duration._
  //
  //  val fm = Monad[Future]
  // error: Could not find an instance of Monad for scala.concurrent.Future
  // val fm = Monad[Future]
  //

  /* ExecutionContextをスコープに入れると、インスタンスを呼び出すために必要な暗黙の解決が修正されます。
  * */

  //  import scala.concurrent.Future
  //  import scala.concurrent.ExecutionContext.Implicits.global
  //
  //  //なんかエラーになるのだが
  //  val fm = Monad[Future]
  //
  //  //Monadインスタンスは、キャプチャされたExecutionContextを使用して、pureおよびflatMapへの後続の呼び出しを行います。
  //
  //  import scala.concurrent.Await
  //  import scala.concurrent.duration.DurationInt
  //
  //  val future = fm.flatMap(fm.pure(1))(x => fm.pure(x + 2))
  //
  //  Await.result(future, 1.second)

  //fm: Monad[Future] = cats.instances.FutureInstances$$anon$1@5493a1be

  /* 上記に加えて、Catsは、標準ライブラリにはない新しいモナドのホストを提供します。 これらのいくつかについては、すぐに理解します。
  * 4.2.3 Monad Syntax
  * モナドの構文は次の3つの場所から来ています。
  * - cats.syntax.flatMapは、flatMapの構文を提供します。
  * - cats.syntax.functorは、マップの構文を提供します。
  * - cats.syntax.applicativeは、pureの構文を提供します。
  * 実際には、cats.implicitsからすべてを一度にインポートする方が簡単なことがよくあります。
  * ただし、わかりやすくするために、ここでは個々のインポートを使用します。
  * モナドのインスタンスを構築するためにpureを使用できます。
  * 必要な特定のインスタンスを明確にするために、typeパラメータを指定する必要があることがよくあります。
  * */

  //  import cats.instances.option._   // for Monad
  //  import cats.instances.list._     // for Monad
  //  import cats.syntax.applicative._ // for pure
  //
  //  1.pure[Option]
  //  // res5: Option[Int] = Some(1)
  //  1.pure[List]
  //  // res6: List[Int] = List(1)

  /* flatMapメソッドとmapメソッドは、それらのメソッドの独自の明示的なバージョンを定義しているため、
  * OptionやListなどのScalaモナドで直接デモンストレーションすることは困難です。
  * 代わりに、ユーザーが選択したモナドにラップされたパラメーターに対して計算を実行する汎用関数を記述します。
  * */


  //行列の計算？

  //  import cats.Monad
  //  import cats.syntax.functor._ // for map
  //  import cats.syntax.flatMap._ // for flatMap
  //
  //  def sumSquare[F[_] : Monad](a: F[Int], b: F[Int]): F[Int] =
  //    a.flatMap(x => b.map(y => x * x + y * y))
  //
  //  import cats.instances.option._ // for Monad
  //  import cats.instances.list._ // for Monad
  //
  //  sumSquare(Option(3), Option(4))
  //  // res7: Option[Int] = Some(25)
  //  sumSquare(List(1, 2, 3), List(4, 5))
  //  // res8: List[Int] = List(17, 26, 20, 29, 25, 34)

  /* catsのmonadの一般性について知る必要があるのは、多かれ少なかれそれだけです。
  * それでは、Scala標準ライブラリには見られなかったいくつかの便利なモナドインスタンスを見てみましょう。
  * 4.3 The Identity Monad
  * 前のセクションでは、さまざまなモナドを抽象化するメソッドを記述して、CatsのflatMapとマップの構文を示しました。
  * */

  //  import cats.Monad
  //  import cats.syntax.functor._ // for map
  //  import cats.syntax.flatMap._ // for flatMap
  //
  //  def sumSquareByFor[F[_] : Monad](a: F[Int], b: F[Int]): F[Int] =
  //    for {
  //      x <- a
  //      y <- b
  //    } yield x * x + y * y

  //このメソッドはオプションとリストでうまく機能しますが、プレーンな値を渡すとは言えません。

  //sumSquare(3, 4)

  /*
  * モナドにあるか、モナドにまったくないパラメーターでsumSquareを使用できれば、非常に便利です。
  * これにより、モナドコードと非モナドコードを抽象化できます。 幸い、Catsはギャップを埋めるためにIdタイプを提供しています。
  * */

  //  import cats.Id
  //
  //  sumSquare(3: Id[Int], 4: Id[Int])
  //  // res1: Id[Int] = 25

  /*
  * Idを使用すると、プレーンな値を使用してモナドメソッドを呼び出すことができます。
  * ただし、正確なセマンティクス(意味論)を理解することは困難です。
  * パラメータをsumSquareにId [Int]としてキャストし、結果としてId [Int]を受け取りました。
  * どうしたの？ 説明するIdの定義は次のとおりです。
  * */

  //  package cats
  //
  //  type Id[A] = A

  /*
  * Idは、実際には、アトミック型を単一パラメーター型コンストラクターに変換する型エイリアスです。
  * 任意のタイプの任意の値を対応するIDにキャストできます。
  * */

  //  "Dave" : Id[String]
  //  // res2: Id[String] = "Dave"
  //  123 : Id[Int]
  //  // res3: Id[Int] = 123
  //  List(1, 2, 3) : Id[List[Int]]
  //  // res4: Id[List[Int]] = List(1, 2, 3)

  /*
  * Catsは、FunctorやMonadなど、Idのさまざまな型クラスのインスタンスを提供します。
  * これらにより、map、flatMap、およびプレーン値の純粋な受け渡しを呼び出すことができます。
  * */

  //  val a = Monad[Id].pure(3)
  //  // a: Id[Int] = 3
  //  val b = Monad[Id].flatMap(a)(_ + 1)
  //  // b: Id[Int] = 4
  //
  //  import cats.syntax.functor._ // for map
  //  import cats.syntax.flatMap._ // for flatMap
  //
  //  for {
  //    x <- a
  //    y <- b
  //  } yield x + y
  //  // res5: Id[Int] = 7

  /*
  * モナドコードと非モナドコードを抽象化する機能は非常に強力です。
  * たとえば、Futureを使用して本番環境で非同期にコードを実行し、Idを使用してテストで同期的にコードを実行できます。
  * これは、第8章の最初のケーススタディで確認できます。
  * 4.3.1 Exercise: Monadic Secret Identities
  * Idにpure、map、flatMapを実装してください！ 実装についてどのような興味深い発見がありますか？
  * */

  import cats.Id

  //次に、各メソッドを順番に見ていきましょう。 純粋な操作では、AからId [A]が作成されます。ただし、AとId [A]は同じタイプです。
  // 私たちがしなければならないのは、初期値を返すことだけです。
  def pure[A](value: A): Id[A] = value

  //ここから答え見た
  /*
  * mapメソッドは、タイプId [A]のパラメーターを受け取り、タイプA => Bの関数を適用して、Id [B]を返します。
  * しかし、Id [A]は単にAであり、Id [B]は単にBです！ 関数を呼び出すだけで、ボックス化やボックス化解除は必要ありません。
  * */
  def map[A, B](initial: Id[A])(func: A => B): Id[B] = func(initial)

  /* 最後のオチは、Id型コンストラクターを取り除くと、flatMapとmapは実際には同一になるということです。 */
  def flatMap[A, B](initial: Id[A])(func: A => Id[B]): Id[B] = func(initial)

  /*
  * これは、シーケンス型クラスとしてのファンクターとモナドの理解と結びついています。
  * 各型クラスを使用すると、ある種の複雑さを無視して操作を順序付けることができます。
  * Idの場合、複雑さはなく、mapとflatMapを同じものにします。
  * 上記のメソッド本体に型アノテーションを記述する必要がないことに注意してください。
  * コンパイラーは、タイプAの値をId [A]として解釈でき、その逆も同様です。
  * これに対する唯一の制限は、Scalaが暗黙的な検索時に型と型コンストラクターを統合できないことです。
  * したがって、このセクションの冒頭でsumSquareを呼び出す際に、IntをId [Int]として再入力する必要があります。
  * */

  /* 4.4 Either
  * もう1つの便利なモナドを見てみましょう。Scala標準ライブラリのEitherタイプです。
  * Scala 2.11以前では、mapメソッドとflatMapメソッドがなかったため、多くの人がどちらのモナドも考慮していませんでした。
  * ただし、Scala 2.12では、どちらかが右バイアスになりました。
  *
  * 4.4.1 Left and Right Bias
  * Scala 2.11では、デフォルトのマップまたはflatMapメソッドがありませんでした。
  * これにより、Scala 2.11バージョンのEitherは、理解のために使用するのに不便になりました。
  * すべてのジェネレーター句に.rightへの呼び出しを挿入する必要がありました。
  *
  * */

  val either1: Either[String, Int] = Right(10)
  val either2: Either[String, Int] = Right(32)

  for {
    a <- either1.right
    b <- either2.right
  } yield a + b

  /*
  * Scala 2.12では、Eitherが再設計されました。
  * 現代のEitherは、右側が成功事例を表すと判断し、mapとflatMapを直接サポートします。
  * これにより、理解がはるかに快適になります。
  * */

  for {
    a <- either1
    b <- either2
  } yield a + b

  /*
  * Catsは、cats.syntax.etherインポートを介してこの動作をScala 2.11にバックポートし、
  * サポートされているすべてのバージョンのScalaで右バイアスのEitherを使用できるようにします。
  * Scala 2.12+では、このインポートを省略するか、何も壊さずにそのままにしておくことができます。
  * */

  import cats.syntax.either._ // for map and flatMap

  for {
    a <- either1
    b <- either2
  } yield a + b

  /* 4.4.2 Creating Instances
  * LeftとRightのインスタンスを直接作成することに加えて、
  * cats.syntax.etherからasLeftおよびasRight拡張メソッドをインポートすることもできます。
  * */

  import cats.syntax.either._ // for asRight

  val a = 3.asRight[String]
  // a: Either[String, Int] = Right(3)
  val b = 4.asRight[String]
  // b: Either[String, Int] = Right(4)

  for {
    x <- a
    y <- b
  } yield x * x + y * y

  /*
  * これらの「スマートコンストラクター」は、LeftとRightではなくEitherタイプの結果を返すため、
  * Left.applyやRight.applyよりも優れています。
  * これは、以下の例の問題のように、絞り込みすぎによって引き起こされる型推論の問題を回避するのに役立ちます。
  * */

  //  def countPositive(nums: List[Int]) =
  //    nums.foldLeft(Right(0)) { (accumulator, num) =>
  //      if(num > 0) {
  //        accumulator.map(_ + 1)
  //      } else {
  //        Left("Negative. Stopping!")
  //      }
  //    }
  // error: type mismatch;
  //  found   : scala.util.Either[Nothing,Int]
  //  required: scala.util.Right[Nothing,Int]
  //       accumulator.map(_ + 1)
  //       ^^^^^^^^^^^^^^^^^^^^^^
  // error: type mismatch;
  //  found   : scala.util.Left[String,Nothing]
  //  required: scala.util.Right[Nothing,Int]
  //       Left("Negative. Stopping!")
  //       ^^^^^^^^^^^^^^^^^^^^^^^^^^^

  /*
  * このコードは、次の2つの理由でコンパイルに失敗します。
  * 1.コンパイラは、アキュムレータのタイプをどちらかではなく右として推測します。
  * 2. Right.applyの型パラメーターを指定しなかったため、コンパイラーは左側のパラメーターをNothingとして推測します。
  * asRightに切り替えると、これらの問題の両方を回避できます。
  * asRightの戻り値の型はEitherであり、1つの型パラメーターのみで型を完全に指定できます。
  * (めっちゃ便利じゃん！)
  * */

  def countPositive(nums: List[Int]) =
    nums.foldLeft(0.asRight[String]) { (accumulator, num) =>
      if (num > 0) {
        accumulator.map(_ + 1)
      } else {
        Left("Negative. Stopping!")
      }
    }

  countPositive(List(1, 2, 3))
  // res5: Either[String, Int] = Right(3)
  countPositive(List(1, -2, 3))
  // res6: Either[String, Int] = Left("Negative. Stopping!")

  /*
  * cats.syntax.etherは、Eitherコンパニオンオブジェクトにいくつかの便利な拡張メソッドを追加します。
  * catchOnlyメソッドとcatchNonFatalメソッドは、次のいずれかのインスタンスとして例外をキャプチャするのに最適です。
  * */

  Either.catchOnly[NumberFormatException]("foo".toInt)
  // res7: Either[NumberFormatException, Int] = Left(
  //   java.lang.NumberFormatException: For input string: "foo"
  // )
  Either.catchNonFatal(sys.error("Badness"))
  // res8: Either[Throwable, Nothing] = Left(java.lang.RuntimeException: Badness)

  //他のデータ型からEitherを作成する方法もあります。(これ便利だなー
  Either.fromTry(scala.util.Try("foo".toInt))
  // res9: Either[Throwable, Int] = Left(
  //   java.lang.NumberFormatException: For input string: "foo"
  // )
  Either.fromOption[String, Int](None, "Badness")
  // res10: Either[String, Int] = Left("Badness")

  /* 4.4.3 Transforming Eithers
  * cats.syntax.etherは、Eitherのインスタンスに役立つメソッドもいくつか追加します。
  * Scala 2.11または2.12のユーザーは、orElseおよびgetOrElseを使用して、右側から値を抽出するか、デフォルトを返すことができます。
  * (すごいおもしろい)
  * */

  import cats.syntax.either._

  "Error".asLeft[Int].getOrElse(0)
  // res11: Int = 0
  "Error".asLeft[Int].orElse(2.asRight[String])
  // res12: Either[String, Int] = Right(2)

  /* sureメソッドを使用すると、右側の値が述語を満たしているかどうかを確認できます。 */
  (-1).asRight[String].ensure("Must be non-negative!")(_ > 0)
  // res13: Either[String, Int] = Left("Must be non-negative!")

  /* RecoverメソッドとrecoverWithメソッドは、Futureの同名のメソッドと同様のエラー処理を提供します。 */

  "error".asLeft[Int].recover {
    case _: String => -1
  }
  // res14: Either[String, Int] = Right(-1)

  "error".asLeft[Int].recoverWith {
    case _: String => Right(-1)
  }
  // res15: Either[String, Int] = Right(-1)

  //mapを補完するleftMapメソッドとbimapメソッドがあります。(flatMapでいいかんじにできるわけではないのか...)

  "foo".asLeft[Int].leftMap(_.reverse)
  // res16: Either[String, Int] = Left("oof")
  6.asRight[String].bimap(_.reverse, _ * 7)
  // res17: Either[String, Int] = Right(42)
  "bar".asLeft[Int].bimap(_.reverse, _ * 7)
  // res18: Either[String, Int] = Left("rab")

  //swapメソッドを使用すると、左から右に交換できます。

  123.asRight[String]
  // res19: Either[String, Int] = Right(123)
  123.asRight[String].swap
  // res20: Either[Int, String] = Left(123)

  //最後に、Catsは、toOption、toList、toTry、toValidatedなどの多数の変換メソッドを追加します。

  /*
  * 4.4.4 Error Handling
  * どちらも通常、フェイルファストエラー処理を実装するために使用されます。
  * 通常どおり、flatMapを使用して計算をシーケンスします。 1つの計算が失敗した場合、残りの計算は実行されません。
  * */

  for {
    a <- 1.asRight[String]
    b <- 0.asRight[String]
    c <- if (b == 0) "DIV0".asLeft[Int]
    else (a / b).asRight[String]
  } yield c * 100
  // res21: Either[String, Int] = Left("DIV0")

  //エラー処理にEitherを使用する場合、エラーを表すために使用するタイプを決定する必要があります。 これにはThrowableを使用できます。

  type Result[A] = Either[Throwable, A]

  /*
  * これにより、scala.util.Tryと同様のセマンティクスが得られます。
  * ただし、問題は、Throwableが非常に幅広いタイプであるということです。
  * どのタイプのエラーが発生したかについては（ほとんど）わかりません。
  * 別のアプローチは、プログラムで発生する可能性のあるエラーを表す代数的データ型を定義することです。
  * */

  //TODO: ProductとSerializableが何かわからない。質問する
  sealed trait LoginError extends Product with Serializable

  final case class UserNotFound(username: String)
    extends LoginError

  final case class PasswordIncorrect(username: String)
    extends LoginError

  case object UnexpectedError extends LoginError

  case class User(username: String, password: String)

  type LoginResult = Either[LoginError, User]

  /*
  * このアプローチは、Throwableで見た問題を解決します。
  * これにより、予想されるエラータイプの固定セットと、予想外のその他のすべてのキャッチオールが提供されます。
  * また、私たちが行うパターンマッチングの網羅性チェックの安全性も得られます。
  * */

  // Choose error-handling behaviour based on type:
  def handleError(error: LoginError): Unit =
    error match {
      case UserNotFound(u) =>
        println(s"User not found: $u")

      case PasswordIncorrect(u) =>
        println(s"Password incorrect: $u")

      case UnexpectedError =>
        println(s"Unexpected error")
    }

  val result1: LoginResult = User("dave", "passw0rd").asRight
  // result1: LoginResult = Right(User("dave", "passw0rd"))
  val result2: LoginResult = UserNotFound("dave").asLeft
  // result2: LoginResult = Left(UserNotFound("dave"))

  result1.fold(handleError, println)
  // User(dave,passw0rd)
  result2.fold(handleError, println)

  // User not found: dave

  /* 4.4.5 Exercise: What is Best?
  * 前の例のエラー処理戦略は、すべての目的に適していますか？ エラー処理に他にどのような機能が必要ですか？
  * */

  //loginできました的なprintlnが必要？

  /*
  * これは未解決の質問です。 これは一種のトリック質問でもあります。答えは、探しているセマンティクスによって異なります。 熟考するいくつかのポイント：
  * - 大規模なジョブを処理する場合、エラー回復は重要です。 1日間ジョブを実行した後、最後の要素で失敗したことを確認したくありません。
  * - エラー報告も同様に重要です。 何かがうまくいかなかったというだけでなく、何がうまくいかなかったのかを知る必要があります。
  * - 多くの場合、最初に発生したエラーだけでなく、すべてのエラーを収集する必要があります。 典型的な例は、Webフォームの検証です。
  *   ユーザーがフォームを送信するときにすべてのエラーを報告する方が、一度に1つずつ報告するよりもはるかに優れたエクスペリエンスです。
  *   (セキュリティ上それはどうなのか？クラッカーに何が間違っているかを教えるのは問題なのでは？)
  * */

  /* 4.5 Aside: Error Handling and MonadError
  * Catsは、エラー処理に使用されるEitherのようなデータ型を抽象化するMonadErrorと呼ばれる追加の型クラスを提供します。
  * MonadErrorは、エラーを発生させて処理するための追加の操作を提供します。
  *
  * このセクションはオプションです！
  * エラー処理モナドを抽象化する必要がない限り、MonadErrorを使用する必要はありません。
  * たとえば、MonadErrorを使用して、FutureとTry、またはEitherとEitherT（第5章で説明します）を抽象化できます。
  * この種の抽象化が今必要ない場合は、セクション4.6に進んでください。
  * 4.5.1 The MonadError Type Class
  * MonadErrorの定義の簡略版を次に示します。
  * * */

  //  trait MonadError[F[_], E] extends Monad[F] {
  //    //エラーを `F`コンテキストに持ち上げます。
  //    def raiseError[A](e: E): F[A]
  //
  //    //エラーを処理し、エラーから回復する可能性があります。
  //    def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
  //
  //    // すべてのエラーを処理し、それらから回復します。
  //    def handleError[A](fa: F[A])(f: E => A): F[A]
  //
  //    //`F`のインスタンスをテストします。
  //    // 述語が満たされない場合は失敗します：
  //    def ensure[A](fa: F[A])(e: E)(f: A => Boolean): F[A]
  //  }

  /*
  * MonadErrorは、次の2つのタイプパラメータで定義されます。
  * - Fはモナドのタイプです。
  * - Eは、Fに含まれるエラーのタイプです。
  * これらのパラメータがどのように組み合わされるかを示すために、Eitherの型クラスをインスタンス化する例を次に示します。
  * */

  import cats.MonadError
  import cats.instances.either._ // for MonadError

  type ErrorOr[A] = Either[String, A]

  val monadError: MonadError[ErrorOr, String] = MonadError[ErrorOr, String]

  /*
  * ApplicativeError 実際には、MonadErrorはApplicativeErrorと呼ばれる別の型クラスを拡張します。
  * ただし、Applicativeは第6章まで出会うことはありません。
  * セマンティクスは各型クラスで同じであるため、ここではこの詳細を無視できます。
  *
  * 4.5.2 Raising and Handling Errors
  * MonadErrorの2つの最も重要なメソッドは、raiseErrorとhandleErrorWithです。
  * raiseErrorは、失敗を表すインスタンスを作成することを除いて、Monadの純粋メソッドに似ています。
  * */

  val success: ErrorOr[Int] = monadError.pure(42)
  // success: ErrorOr[Int] = Right(42)
  val failure: ErrorOr[Nothing] = monadError.raiseError("Badness")
  // failure: ErrorOr[Nothing] = Left("Badness")

  /*
  * handleErrorWithは、raiseErrorを補完するものです。
  * これにより、Futureのrecoverメソッドと同様に、エラーを消費し、（おそらく）成功に変えることができます。
  * */

  monadError.handleErrorWith(failure) {
    case "Badness" =>
      monadError.pure("It's ok")

    case _ =>
      monadError.raiseError("It's not ok")
  }
  // res0: ErrorOr[String] = Right("It's ok")

  // 考えられるすべてのエラーを処理できることがわかっている場合は、handleWithを使用できます。

  //  monadError.handleError(failure) {
  //    case "Badness" => 42
  //
  //    case _ => -1
  //  }
  //
  /*
  * フィルタのような動作を実装するensureと呼ばれる別の便利な方法があります。
  * 成功したモナドの値を述語でテストし、述語がfalseを返した場合に発生するエラーを指定します。
  * */

  monadError.ensure(success)("Number too low!")(_ > 1000)

  /*
  * Catsは、cats.syntax.applicativeErrorを介してraiseErrorおよびhandleErrorWithの構文を提供し
  * cats.syntax.monadErrorを介して確認します。
  * */

  //  import cats.syntax.applicative._ // for pure
  //  import cats.syntax.applicativeError._ // for raiseError etc
  //  import cats.syntax.monadError._ // for ensure
  //
  //  val success = 42.pure[ErrorOr]
  //  // success: ErrorOr[Int] = Right(42)
  //  val failure = "Badness".raiseError[ErrorOr, Int]
  //  // failure: ErrorOr[Int] = Left("Badness")
  //  failure.handleErrorWith {
  //    case "Badness" =>
  //      256.pure
  //
  //    case _ =>
  //      ("It's not ok").raiseError
  //  }
  //  // res4: ErrorOr[Int] = Right(256)
  //  success.ensure("Number to low!")(_ > 1000)
  //  // res5: ErrorOr[Int] = Left("Number to low!")

  /*
  * これらのメソッドには他にも便利なバリエーションがあります。
  * 詳細については、cats.MonadErrorおよびcats.ApplicativeErrorのソースを参照してください。
  *
  * */


  def main(args: Array[String]): Unit = {
    println(parseInt("abc"))
    println(parseInt("123"))
  }

}
