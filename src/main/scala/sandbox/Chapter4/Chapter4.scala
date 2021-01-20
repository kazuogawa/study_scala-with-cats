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

  def main(args: Array[String]): Unit = {
    println(parseInt("abc"))
    println(parseInt("123"))
  }

}
