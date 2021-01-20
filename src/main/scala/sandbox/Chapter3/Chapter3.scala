package sandbox.Chapter3


object Chapter3 {
  /*3. Functor
  * この章では、ファンクター、つまりリスト、オプション、
  * またはその他の1000の可能性のいずれかなどのコンテキスト内で
  * 一連の操作を表すことを可能にする抽象化について調査します。
  * ファンクター自体はそれほど有用ではありませんが、
  * モナドやアプリケーションファンクターなどのファンクターの特殊なケースは、
  * Catsで最も一般的に使用される抽象化の一部です。
  * */

  /* 3.1 Examples of Functors
  * 非公式には、ファンクターはマップメソッドを持つものです。
  * あなたはおそらくこれを持っている多くのタイプを知っているでしょう：いくつか例を挙げると、Option、List、Either。
  * 通常、リストを反復処理するときに最初にマップに遭遇します。
  * ただし、ファンクターを理解するには、別の方法でメソッドを考える必要があります。
  * Listをtraverseするのではなく、リスト内のすべての値を一度に変換するものと考える必要があります。
  * 適用する関数を指定し、mapはそれがすべてのアイテムに適用されることを保証します。
  * 値は変更されますが、リストの構造（要素の数とその順序）は同じままです。
  * */

  List(1, 2, 3).map(n => n + 1)

  /*
  * 同様に、Optionにマップする場合、コンテンツを変換しますが、SomeまたはNoneコンテキストは変更しません。
  * 同じ原則が、LeftコンテキストとRightコンテキストのどちらにも当てはまります。
  * この変換の一般的な概念は、図1に示すタイプ署名の一般的なパターンとともに、さまざまなデータタイプ間でマップの動作を結び付けるものです。
  *
  * マップはコンテキストの構造を変更しないままにするため、マップを繰り返し呼び出して
  * 初期データ構造のコンテンツに対する複数の計算をシーケンスすることができます。
  * */

  List(1, 2, 3).
    map(n => n + 1).
    map(n => n * 2).
    map(n => s"${n}!")

  /*
  * マップは、反復パターンとしてではなく、関連するデータ型によって決定される複雑さを無視して、
  * 値の計算を順序付ける方法として考える必要があります。
  * Option-値が存在する場合と存在しない場合があります。
  * Either-値またはエラーがある可能性があります。
  * List-値が0個以上ある場合があります。
  * */

  /*3.2 More Examples of Functors
  * List、Option、Eitherのmapメソッドは、関数を熱心に適用します。
  * ただし、シーケンス計算の考え方はこれよりも一般的です。 パターンをさまざまな方法で適用する他のファンクターの動作を調べてみましょう。
  *
  * Future
  * Futureは、非同期計算をキューに入れ、前任者(predecessors)が完了するときにそれらを適用することによって、
  * 非同期計算をシーケンスするファンクターです。
  * 図2に示す、そのmapメソッドの型シグネチャは、上記のシグネチャと同じ形状です。 ただし、動作は大きく異なります。
  *
  * Futureで作業する場合、その内部状態についての保証はありません。
  * ラップされた計算は、進行中、完全、または拒否される可能性があります。
  * Futureが完了すると、マッピング関数をすぐに呼び出すことができます。
  * そうでない場合、一部の基になるスレッドプールは関数呼び出しをキューに入れ、後で戻ってきます。
  * 関数がいつ呼び出されるかはわかりませんが、関数が呼び出される順序はわかります。
  * このように、Futureは、リスト、オプション、および次のいずれかに見られるのと同じシーケンス動作を提供します。
  * */

  //import scala.concurrent.{Future, Await}
  //import scala.concurrent.ExecutionContext.Implicits.global
  //import scala.concurrent.duration._

  //def main(args: Array[String]): Unit = {
  //  val future: Future[String] =
  //    Future(123).
  //      map(n => n + 1).
  //      map(n => n * 2).
  //      map(n => s"${n}!")

  //  //printlnしないとdiscarded non-Unit valueが出るので注意。()で終わらせてもいいかも
  //  //https://labs.septeni.co.jp/entry/2017/02/16/113949
  //  println(Await.result(future, 1.second))
  //}

  /*
  * 先物と参照透過性
  * ScalaのFuturesは、参照透過性がないため、純粋な関数型プログラミングの優れた例ではないことに注意してください。
  * Futureは常に結果を計算してキャッシュし、この動作を微調整する方法はありません。
  * これは、Futureを使用して副作用のある計算をラップすると、予測できない結果が得られる可能性があることを意味します。 例えば：
  *
  * */

  //  import scala.concurrent.{Future, Await}
  //  import scala.concurrent.ExecutionContext.Implicits.global
  //  import scala.concurrent.duration._
  //  import scala.util.Random
  //  def main(args: Array[String]): Unit = {
  //
  //    val future1 = {
  //      // Initialize Random with a fixed seed:
  //      val r = new Random(0L)
  //
  //      // nextInt has the side-effect of moving to
  //      // the next random number in the sequence:
  //      val x = Future(r.nextInt)
  //
  //      for {
  //        a <- x
  //        b <- x
  //      } yield (a, b)
  //    }
  //
  //    val future2 = {
  //      val r = new Random(0L)
  //
  //      for {
  //        a <- Future(r.nextInt)
  //        b <- Future(r.nextInt)
  //      } yield (a, b)
  //    }
  //
  //    val result1 = Await.result(future1, 1.second)
  //    // result1: (Int, Int) = (-1155484576, -1155484576)
  //    println(result1)
  //    val result2 = Await.result(future2, 1.second)
  //    // result2: (Int, Int) = (-1155484576, -723955400)
  //    println(result2)
  //  }

  /*
  * 理想的には、result1とresult2に同じ値を含める必要があります。
  * ただし、future1の計算ではnextIntが1回呼び出され、future2の計算では2回呼び出されます。
  * nextIntは、それぞれの場合に異なる結果を取得するたびに異なる結果を返すためです。
  * この種の不一致は、先物と副作用を含むプログラムについて推論することを困難にします。
  * また、Futureの動作には、プログラムをいつ実行するかをユーザーが指示するのではなく、
  * 常にすぐに計算を開始する方法など、他にも問題のある側面があります。 詳細については、RobNorrisによるこの優れたRedditの回答を参照してください。
  * Cats Effectを見ると、IOタイプがこれらの問題を解決していることがわかります。
  * Futureが参照透過性でない場合は、別の同様のデータ型を調べる必要があります。 あなたはこれを認識する必要があります…
  * */

  /*
  * 単一の引数関数もファンクターであることがわかります。
  * これを確認するには、タイプを少し調整する必要があります。
  * 関数A => Bには、パラメータータイプAと結果タイプBの2つのタイプパラメーターがあります。
  * それらを正しい形状に強制するために、パラメータータイプを修正し、結果タイプを変化させることができます。
  * X => Aで開始します。
  * 関数A => Bを提供します。
  * X => Bに戻ります。
  * X => AをMyFunc [A]とエイリアスすると、この章の他の例で見たのと同じタイプのパターンが表示されます。 これは図3にも見られます。
  * MyFunc [A]で開始します。
  * 関数A => Bを提供します。
  * MyFunc [B]を取り戻します。
  *
  * 言い換えると、Function1の「マッピング」は、関数の合成です。
  * */

  //  import cats.instances.function._ // for Functor
  //  import cats.syntax.functor._ // for map
  //
  //  val func1: Int => Double =
  //    (x: Int) => x.toDouble
  //
  //  val func2: Double => Double =
  //    (y: Double) => y * 2
  //
  //  def main(args: Array[String]): Unit = {
  //    println((func1 map func2) (1)) // composition using map
  //    // res3: Double = 2.0     // composition using map
  //    //andThenは関数合成
  //    println((func1 andThen func2) (1)) // composition using andThen
  //    // res4: Double = 2.0 // composition using andThen
  //    //なんかこの書き方が一番なれている
  //    println(func2(func1(1))) // composition written out by hand
  //    // res5: Double = 2.0
  //
  //  }

  /*
  * これは、シーケンス操作の一般的なパターンとどのように関連していますか？
  * 考えてみると、関数の合成はシーケンスです。
  * まず、単一の操作を実行する関数から始め、マップを使用するたびに、チェーンに別の操作を追加します。
  * mapを呼び出しても実際にはどの操作も実行されませんが、
  * 最後の関数に引数を渡すことができれば、すべての操作が順番に実行されます。
  * これは、Futureと同様の操作を遅延キューイングすることと考えることができます。
  * */

  //scalaを2.13より古い状態でbuild.sbtに下記を描いていれば動く
  //scalacOptions += "-Ypartial-unification"が必要
  //def main(args: Array[String]): Unit = {
  //  val func =
  //    ((x: Int) => x.toDouble).
  //      map(x => x + 1).
  //      map(x => x * 2).
  //      map(x => s"${x}!")

  //  println(func(123))
  //  // res6: String = "248.0!"
  //}

  /* 3.3 Definition of a Functor
  * これまで見てきたすべての例はファンクターです。
  * つまり、シーケンス計算をカプセル化するクラスです。
  * 正式には、ファンクターはタイプF [A]であり、タイプ（A => B）=> F [B]の操作マップがあります。 一般的なタイプチャートを図4に示します。
  * CatsはFunctorを型クラスcats.Functorとしてエンコードするため、メソッドの外観は少し異なります。
  * 初期のF [A]を変換関数と一緒にパラメーターとして受け入れます。 定義の簡略版は次のとおりです。
  * */

  //package cats
  //trait Functor[F[_]] {
  //  def map[A, B](fa: F[A])(f: A => B): F[B]
  //}

  /*
  * F [_]のような構文をこれまでに見たことがない場合は、少し回り道をして、型コンストラクターとより高い種類の型について説明します。
  *
  * ファンクターの法則
  * ファンクターは、多くの小さな操作を1つずつシーケンスする場合でも、
  * mappingする前にそれらを組み合わせてより大きな関数にする場合でも、同じセマンティクスを保証します。
  * これを確実に行うには、次の法律を遵守する必要があります。
  * アイデンティティ：アイデンティティ関数を使用してマップを呼び出すことは、何もしないことと同じです。
  * fa.map(a => a) == fa
  * 構成：2つの関数fとgを使用したマッピングは、fを使用してマッピングしてからgを使用してマッピングすることと同じです。
  * fa.map(g(f(_)) == fa.map(f).map(g)
  * */

  /* 3.4余談：より高い種類と型コンストラクタ
  * 種類は型の型のようなものです。 タイプの「穴」の数を表します。
  * 穴のない通常の型と、型を生成するために埋めることができる穴のある「型コンストラクター」を区別します。
  * たとえば、Listは1つの穴を持つ型コンストラクターです。
  * List [Int]やList [A]のような通常の型を生成するパラメーターを指定することにより、その穴を埋めます。
  * 秘訣は、型コンストラクターをジェネリック型と混同しないことです。 Listは型コンストラクター、List [A]は型です。
  * List    // type constructor, takes one parameter
  * List[A] // type, produced by applying a type parameter
  *
  * ここには、関数と値との密接な類似性があります。 関数は「値コンストラクター」です。パラメーターを指定すると、関数は値を生成します。
  * math.abs    // function, takes one parameter
  * math.abs(x) // value, produced by applying a value parameter
  *
  * Scalaでは、アンダースコアを使用して型コンストラクターを宣言します。
  * これは、型コンストラクターが持つ「穴」の数を指定します。 ただし、それらを使用するために、名前だけを参照します。
  * // Declare F using underscores:
      def myMethod[F[_]] = {

        // Reference F without underscores:
        val functor = Functor.apply[F]

        // ...
      }
  *
  * これは、関数パラメータータイプの指定に似ています。
  * パラメータを宣言するときは、そのタイプも指定します。 ただし、それらを使用するために、名前だけを参照します。
  * // Declare f specifying parameter types
    def f(x: Int): Int =
      // Reference x without type
      x * 2
  *
  * 型コンストラクターに関するこの知識を身に付ければ、FunctorのCats定義により、List、Option、Futureなどの単一パラメーター型コンストラクター、
  * またはMyFuncなどの型エイリアスのインスタンスを作成できることがわかります。
  * */

  /*
  * 言語機能のインポート
  * 2.13より前のバージョンのScalaでは、A [_]構文で型コンストラクターを宣言するたびに、コンパイラーからの警告を抑制するために、
  * より高い種類の型言語機能を「有効化」する必要があります。
  * 上記の「language import」を使用してこれを行うことができます。
  * import scala.language.higherKinds
  * または、build.sbtのscalacOptionsに以下を追加します。
  * scalacOptions + = "-language：higherKinds"
  * 実際には、scalacOptionsフラグが2つのオプションの中でより単純であることがわかります。
  * */

  /* 3.5 Functors in Cats
  * Catsでのファンクターの実装(implementation)を見てみましょう。
  * モノイドに対して行ったのと同じ側面、つまり型クラス、インスタンス、構文について調べます。
  *
  * 3.5.1 The Functor Type Class and Instances
  * ファンクター型クラスはcats.Functorです。
  * コンパニオンオブジェクトで標準のFunctor.applyメソッドを使用してインスタンスを取得します。
  * いつものように、デフォルトのインスタンスはcats.instancesパッケージのタイプごとに配置されています。
  *
  * */

  import cats.Functor
  //import cats.instances.list._
  //import cats.instances.option._

  //  val list1 = List(1, 2, 3)
  //  // list1: List[Int] = List(1, 2, 3)
  //  val list2 = Functor[List].map(list1)(_ * 2)
  //  // list2: List[Int] = List(2, 4, 6)
  //
  //  val option1 = Option(123)
  //  // option1: Option[Int] = Some(123)
  //  val option2 = Functor[Option].map(option1)(_.toString)
  //  // option2: Option[String] = Some("123")

  /*
  * ファンクターは、リフトと呼ばれるメソッドを提供します。
  * これは、タイプA => Bの関数を、ファンクター上で動作し、タイプF [A] => F [B]を持つ関数に変換します。
  * */
  //  val func = (x: Int) => x + 1
  //  // func: Int => Int = <function1>

  //  val liftedFunc = Functor[Option].lift(func)
  //  // liftedFunc: Option[Int] => Option[Int] = cats.Functor$$Lambda$11546/665425203@13439aca

  //  liftedFunc(Option(1))
  //  // res1: Option[Int] = Some(2)

  /*
  * asメソッドは、使用する可能性が高いもう1つのメソッドです。これは、指定された値を持つファンクター内の値に置き換えられます。
  * (使い道あるのか・・・？)
  * */

  //  Functor[List].as(list1, "As")
  //  // res2: List[String] = List("As", "As", "As")

  /* 3.5.2 Functor Syntax
  * Functorの構文によって提供される主なメソッドはmapです。
  * オプションとリストには独自の組み込みマップメソッドがあり、Scalaコンパイラは常に拡張メソッドよりも組み込みメソッドを優先するため、
  * これをオプションとリストで示すことは困難です。 2つの例でこれを回避します。
  * まず、関数のマッピングを見てみましょう。 ScalaのFunction1タイプにはmapメソッドがないため（代わりにandThenと呼ばれます）、
  * 名前の競合はありません。
  * (andThenとmapって違いがある気がするけど、一緒みたいな言い方でいいのか？関数合成と値の変換って違いのイメージがある)
  * */

  //import cats.instances.function._ // for Functor
  import cats.syntax.functor._ // for map
  //
  //  val func1 = (a: Int) => a + 1
  //  val func2 = (a: Int) => a * 2
  //  val func3 = (a: Int) => s"${a}!"
  //  val func4 = func1.map(func2).map(func3)
  //
  //  func4(123)
  //  // res3: String = "248!"

  /* 別の例を見てみましょう。今回はファンクターを抽象化するので、特定の具体的なタイプは扱いません。
  * ファンクターのコンテキストに関係なく、方程式を数値に適用するメソッドを作成できます。
  * */
  //  def doMath[F[_]](start: F[Int])
  //                  (implicit functor: Functor[F]): F[Int] =
  //    start.map(n => n + 1 * 2)
  //
  //  import cats.instances.option._ // for Functor
  //  import cats.instances.list._   // for Functor
  //
  //  doMath(Option(20))
  //  // res4: Option[Int] = Some(22)
  //  doMath(List(1, 2, 3))
  //  // res5: List[Int] = List(3, 4, 5)

  /* これがどのように機能するかを説明するために、cats.syntax.functorのmapメソッドの定義を見てみましょう。
  コードの簡略版は次のとおりです。
  * */

  //  implicit class FunctorOps[F[_], A](src: F[A]) {
  //    def map[B](func: A => B)
  //              (implicit functor: Functor[F]): F[B] =
  //      functor.map(src)(func)
  //  }

  /*コンパイラーは、この拡張メソッドを使用して、組み込みのマップが使用できない場合にマップメソッドを挿入できます。*/

  //foo.map(value => value + 1)

  /* fooに組み込みのmapメソッドがないと仮定すると、コンパイラーは潜在的なエラーを検出し、式をFunctorOpsでラップして、コードを修正します。 */

  //  new FunctorOps(foo).map(value => value + 1)

  /* FunctorOpsのmapメソッドには、パラメーターとして暗黙のFunctorが必要です。
  つまり、このコードは、スコープにFのファンクターがある場合にのみコンパイルされます。そうしないと、コンパイラエラーが発生します。*/
  final case class Box[A](value: A)

  //
  //  val box = Box[Int](123)
  //
  //  box.map(value => value + 1)
  // error: value map is not a member of repl.Session.App0.Box[Int]
  // box.map(value => value + 1)
  // ^^^^^^^

  //asメソッドは構文としても使用できます。

  //  List(1, 2, 3).as("As")

  /* 3.5.3 Instances for Custom Types
  * マップメソッドを定義するだけでファンクターを定義できます。
  * cats.instancesにはすでにそのようなものが存在しますが、オプションのファンクターの例を次に示します。実装は簡単です。
  * Optionのmapメソッドを呼び出すだけです。
  * */
  //  implicit val optionFunctor: Functor[Option] =
  //    new Functor[Option] {
  //      def map[A, B](value: Option[A])(func: A => B): Option[B] =
  //        value.map(func)
  //    }

  /*
  * インスタンスに依存関係を注入する必要がある場合があります。
  * たとえば、FutureのカスタムFunctorを定義する必要がある場合（別の架空の例-Catsはcats.instances.futureに1つ提供します）、
  * future.mapの暗黙的なExecutionContextパラメーターを考慮する必要があります。
  * functor.mapにパラメータを追加することはできないため、インスタンスを作成するときに依存関係を考慮する必要があります。
  *
  * */

  import scala.concurrent.{Future, ExecutionContext}

  implicit def futureFunctor
  (implicit ec: ExecutionContext): Functor[Future] =
    new Functor[Future] {
      def map[A, B](value: Future[A])(func: A => B): Future[B] =
        value.map(func)
    }

  /*
  * Functor.applyを直接使用するか、マップ拡張メソッドを介して間接的に、Functor for Futureを呼び出すと、
  * コンパイラは暗黙的な解決によってfutureFunctorを見つけ、呼び出しサイトでExecutionContextを再帰的に検索します。
  * これは、拡張がどのように見えるかです。
  * */
  //  // We write this:
  //  Functor[Future]

  //  // The compiler expands to this first:
  //  Functor[Future](futureFunctor)

  //  // And then to this:
  //  Functor[Future](futureFunctor(executionContext))

  /* 3.5.4 Exercise: Branching out with Functors
  * 次の二分木データ型のファンクターを記述します。ブランチとリーフのインスタンスでコードが期待どおりに機能することを確認します。
  * */
  sealed trait Tree[+A]

  final case class Branch[A](left: Tree[A], right: Tree[A])
    extends Tree[A]

  final case class Leaf[A](value: A) extends Tree[A]


  //答え見た。なるほどってなった。
  /* セマンティクスは、リストのファンクターを作成するのと似ています。
    見つかったすべてのリーフに関数を適用して、データ構造を繰り返します。ファンクターの法則は、
    ブランチノードとリーフノードの同じパターンで同じ構造を保持することを直感的に要求しています。
  */

  implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
    override def map[A, B](tree: Tree[A])(f: A => B): Tree[B] = tree match {
      case Branch(left, right) => Branch(map(left)(f), map(right)(f))
      case Leaf(value) => Leaf(f(value))
    }
  }

  /*
  * おっと！これは、セクション1.6.1で説明したのと同じ不変性の問題に反します。
  * コンパイラはTreeのFunctorインスタンスを見つけることができますが、BranchまたはLeafのインスタンスは見つけることができません。
  * これを補うために、いくつかのスマートコンストラクターを追加しましょう。
  * (継承するだけでなくちゃんとobjectとして定義しようねって話)
  * */
  //println(Branch(Leaf(10), Leaf(20)).map(_ * 2))

  object Tree {
    def branch[A](left: Tree[A], right: Tree[A]): Tree[A] =
      Branch(left, right)

    def leaf[A](value: A): Tree[A] =
      Leaf(value)
  }

  println(Tree.leaf(100).map(_ * 2))
  println(Tree.branch(Tree.leaf(10), Tree.leaf(20)).map(_ * 2))


  /* ##対比および対比不変の不変関数
  * これまで見てきたように、Functorのマップメソッドは、チェーンへの変換を「追加」するものと考えることができます。
  * 次に、他の2つの型クラスを見ていきます。1つはチェーンの先頭にある操作を表し、
  * もう1つは双方向の操作チェーンの構築を表します。これらは、それぞれ反変および不変ファンクタと呼ばれます。
  *
  * このセクションはオプションです！ この本で最も重要なパターンであり、次の章の焦点であるモナドを理解するために、
  * 反変および不変のファンクターについて知る必要はありません。
  * ただし、第6章の半グループ的および適用的についての説明では、反変量と不変量が役立ちます。
  * 今すぐモナドに移動したい場合は、第4章に直接スキップしてください。第6章を読む前に、ここに戻ってください。
  * */

  /* 3.5.5  Contravariant Functors and the contramap Method (対比関手と対比マップ法)
  * 最初の型クラスである共変ファンクターは、チェーンに操作を「追加」することを表す、contramapと呼ばれる操作を提供します。
  * 一般的なタイプシグネチャを図5に示します。
  * コントラマップメソッドは、変換を表すデータ型に対してのみ意味があります。
  * たとえば、Option [B]の値を関数A => Bを介して逆方向にフィードする方法がないため、
  * Optionのコントラマップを定義することはできません。ただし、第1章で説明したPrintable型クラスのコントラマップを定義できます。
  * */

  //  trait Printable[A] {
  //    def format(value: A): String
  //  }

  /* Printable [A]は、Aから文字列への変換を表します。
     そのcontramapメソッドは、タイプB => Aの関数funcを受け入れ、新しいPrintable [B]を作成します。
  * */

  //  trait Printable[A] {
  //    def format(value: A): String
  //
  //    def contramap[B](func: B => A): Printable[B] =
  //      ???
  //  }
  //
  //  def format[A](value: A)(implicit p: Printable[A]): String =
  //    p.format(value)

  /* 3.5.5.1 Exercise: Showing off with Contramap
  * 上記のPrintableのcontramapメソッドを実装します。次のコードテンプレートから始めて、???を置き換えます。作業方法本体付き：
  *
  * 行き詰まったら、タイプについて考えてください。
  * タイプBの値を文字列に変換する必要があります。どのような機能と方法を利用でき、どのような順序で組み合わせる必要がありますか？
  * */

  /* これが実際の実装です。 funcを呼び出してBをAに変換し、次に元のPrintableを使用してAを文字列に変換します。
  手先の早業の小さなショーでは、自己エイリアスを使用して、外側と内側のPrintablesを区別します。
  * */
  //こたえみた。そっかselfつければいいのか
  trait Printable[A] {
    self =>
    def format(value: A): String

    def contramap[B](func: B => A): Printable[B] =
      new Printable[B] {
        def format(value: B): String =
          self.format(func(value))
      }
  }

  //テストの目的で、文字列とブール値のPrintableのインスタンスをいくつか定義しましょう。

  implicit val stringPrintable: Printable[String] =
    new Printable[String] {
      def format(value: String): String =
        s"'${value}'"
    }

  implicit val booleanPrintable: Printable[Boolean] =
    new Printable[Boolean] {
      def format(value: Boolean): String =
        if (value) "yes" else "no"
    }

  //  import sandbox.Chapter3.Chapter3.stringPrintable.format
  //
  //  format("hello")
  //  // res2: String = "'hello'"
  //
  //  import sandbox.Chapter3.Chapter3.booleanPrintable.format
  //
  //  format(true)

  // res3: String = "yes"

  /* 次に、次のBoxケースクラスのPrintableのインスタンスを定義します。
  セクション1.2.3で説明されているように、これを暗黙のdefとして記述する必要があります。
  * */

  //  final case class Box[A](value: A)

  /*
  * 完全な定義を最初から書き出すのではなく（new Printable [Box]など）、
  * contramapを使用して既存のインスタンスからインスタンスを作成します。 インスタンスは次のように機能するはずです。
  * */

  //format(Box("hello world"))
  //// res4: String = "'hello world'"
  //format(Box(true))
  // res5: String = "yes"


  //implicit val boxPrintable: Printable[Box] =
  //  new Printable[Box] {
  //    override def format(box: Box): String = box.value match {
  //      case s: String => "'" + s + "'"
  //      case bool: Boolean => if (bool) "yes" else "no"
  //      case _ => throw new Exception("error")
  //    }
  //  }

  /* すべてのタイプのBoxでインスタンスを汎用にするために、Box内のタイプのPrintableに基づいています。
  完全な定義を手作業で書き出すことができます。(確かに) */

  //  implicit def boxPrintable[A](implicit p: Printable[A]): Printable[Box[A]] =
  //    new Printable[Box[A]] {
  //      def format(box: Box[A]): String =
  //        p.format(box.value)
  //    }
  //
  //  /* または、contramapを使用して、暗黙のパラメーターに基づいて新しいインスタンスを作成します。*/
  //  implicit def boxPrintable[A](implicit p: Printable[A]): Printable[Box[A]] =
  //    p.contramap[Box[A]](_.value)

  /* コントラマップの使用ははるかに簡単であり、純粋関数型コンビネータを使用して単純なビルディングブロックを組み合わせることにより、
    ソリューションを構築する関数型プログラミングアプローチを伝えます。
  *
  * */

  /* 3.5.6 Invariant functors and the imap method(不変ファンクターとimapメソッド)
  * 不変ファンクターは、mapとcontramapの組み合わせと非公式に同等のimapと呼ばれるメソッドを実装します。
  * mapがチェーンに関数を追加することによって新しい型クラスインスタンスを生成し、contramapがチェーンに操作を追加することによって
  * それらを生成する場合、imapは双方向変換のペアを介してそれらを生成します。
  * この最も直感的な例は、Play JSONのFormatやscodecのコーデックなど、エンコードとデコードをデータ型として表す型クラスです。
  * 文字列との間のエンコードとデコードをサポートするようにPrintableを拡張することで、独自のコーデックを構築できます。
  * */

  //  trait Codec[A] {
  //    def encode(value: A): String
  //
  //    def decode(value: String): A
  //
  //    def imap[B](dec: A => B, enc: B => A): Codec[B] = ???
  //  }
  //
  //  def encode[A](value: A)(implicit c: Codec[A]): String =
  //    c.encode(value)
  //
  //  def decode[A](value: String)(implicit c: Codec[A]): A =
  //    c.decode(value)

  /* imapのタイプチャートを図6に示します。Codec[A]と関数A => BおよびB => Aのペアがある場合、imapメソッドはCodec [B]を作成します。
  * ユースケースの例として、基本的なコーデック[文字列]があり、
  * そのエンコードメソッドとデコードメソッドの両方が、渡された値を返すだけだと想像してください。
  * */

  implicit val stringCodec: Codec[String] =
    new Codec[String] {
      def encode(value: String): String = value

      def decode(value: String): String = value
    }

  /*
  * imapを使用してstringCodecから構築することにより、他のタイプに役立つ多くのコーデックを構築できます。
  * (単純にcodecのwrapperってことか)
  * */

  //  implicit val intCodec: Codec[Int] =
  //    stringCodec.imap(_.toInt, _.toString)
  //
  //  implicit val booleanCodec: Codec[Boolean] =
  //    stringCodec.imap(_.toBoolean, _.toString)

  /*失敗への対処 コーデックタイプクラスのデコードメソッドは失敗を考慮していないことに注意してください。
  より洗練された関係をモデル化したい場合は、ファンクターを超えてレンズと光学系(lenses and optics)を調べることができます。
  光学はこの本の範囲を超えています。ただし、Julien TruffautのライブラリMonocleは、さらに調査するための優れた出発点となります。
  https://www.optics.dev/Monocle/
  */

  /* 3.5.6.1 Transformative Thinking with imap(imapを使用した変革的思考)
  * 上記のコーデックのimapメソッドを実装しましょう。
  * */
  //答え見た。そういうのでいいのか。。なるほど。stringを通る意味があまりわからんが・・・
  trait Codec[A] {
    self =>
    def encode(value: A): String

    def decode(value: String): A

    def imap[B](dec: A => B, enc: B => A): Codec[B] = new Codec[B] {
      override def encode(value: B): String = self.encode(enc(value))

      override def decode(value: String): B = dec(self.decode(value))
    }
  }

  //Doubleのコーデックを作成して、imapメソッドが機能することを示します。
  //これはできた
  implicit val doubleCodec: Codec[Double] = stringCodec.imap(_.toDouble, _.toString)

  //最後に、次のボックスタイプのコーデックを実装します。
  //これだとtoStringが実装していないとエラーになっちゃうのでだめぽ
  //implicit val boxCodec: Codec[Box] = stringCodec.imap(Box(_), _.value.toString)

  /* 任意のAに対してBox [A]の汎用コーデックが必要です。
  これはCodec [A]でimapを呼び出すことによって作成し、暗黙のパラメーターを使用してスコープに取り込みます。*/
  implicit def boxCodec[A](implicit c: Codec[A]): Codec[Box[A]] =
    c.imap[Box[A]](Box(_), _.value)

  /* 名前とは何ですか？
  「反変性」、「不変性」、「共分散」という用語と、これらのさまざまな種類のファンクターとの関係は何ですか？
   セクション1.6.1を思い出してください。分散はサブタイプに影響します。
   これは基本的に、コードを壊すことなく、あるタイプの値を別のタイプの値の代わりに使用する機能です。
   サブタイピングは変換と見なすことができます。 BがAのサブタイプである場合、いつでもBをAに変換できます。
   同様に、関数B => Aが存在する場合、BはAのサブタイプであると言えます。
   標準の共変ファンクターはこれを正確にキャプチャします。 Fが共変関手である場合、
   F [B]と変換B => Aがある場合は常に、いつでもF [A]に変換できます。
   共変ファンクターは反対のケースをキャプチャします。
   Fが共変関手である場合、F [A]と変換B => Aがあるときはいつでも、F [B]に変換できます。
   最後に、不変関手は、関数A => Bを介してF [A]からF [B]に、または関数B => Aを介してその逆に変換できる場合をキャプチャします。
  * */

  /* 3.6 Contravariant and Invariant in Cats(Catsの共変と反変)
  * cats.Contravariantおよびcats.Invariant型クラスによって提供される、
  * Catsでの反変および不変ファンクターの実装を見てみましょう。コードの簡略版は次のとおりです。
  * */

  //  trait Contravariant[F[_]] {
  //    def contramap[A, B](fa: F[A])(f: B => A): F[B]
  //  }
  //
  //  trait Invariant[F[_]] {
  //    def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]
  //  }

  /* 3.6.1 Contravariant in Cats
  * Contravariant.applyメソッドを使用して、Contravariantのインスタンスを呼び出すことができます。
  * Catsは、Eq、Show、Function1などのパラメーターを消費するデータ型のインスタンスを提供します。次に例を示します。
  * */

  import cats.Contravariant
  import cats.Show
  import cats.instances.string._

  val showString: Show[String] = Show[String]

  val showSymbol: Show[Symbol] = Contravariant[Show].contramap(showString)((sym: Symbol) => s"'${sym.name}")

  showSymbol.show(Symbol("dave"))
  // res1: String = "'dave"

  /*
  * より便利なことに、cats.syntax.contravariantを使用できます。これは、contramap拡張メソッドを提供します。
  * */

  import cats.syntax.contravariant._ // for contramap

  showString
    .contramap[Symbol](sym => s"'${sym.name}")
    .show(Symbol("dave"))
  // res2: String = "'dave"

  /* 3.6.2 Invariant in Cats
  * 他のタイプの中で、CatsはMonoidのInvariantのインスタンスを提供します。
  * これは、セクション3.5.6で紹介したコーデックの例とは少し異なります。あなたが思い出すなら、これはMonoidがどのように見えるかです：
  * */
  //  trait Monoid[A] {
  //    def empty: A
  //    def combine(x: A, y: A): A
  //  }

  /*
  * ScalaのSymbolタイプのモノイドを作成したいとします。
  * CatsはSymbolのモノイドを提供していませんが、同様のタイプであるStringのMonoidを提供しています。
  * 空の文字列に依存する空のメソッドと、次のように機能する結合メソッドを使用して、新しいsemigroupを記述できます。
  * 1. パラメータとして2つのシンボルを受け入れます。
  * 2. 記号を文字列に変換します。
  * 3. Monoid [String]を使用して文字列を結合します。
  * 4. 結果をシンボルに変換し直します。
  * type
  * String => Symbolおよび
  * Symbol => Stringの関数をパラメーターとして渡して、imapを使用してcombineを実装できます。
  * cats.syntax.invariantが提供するimap拡張メソッドを使用して記述されたコードは次のとおりです。
  * */

  //  import cats.Monoid
  //  import cats.instances.string._ // for Monoid
  //  import cats.syntax.invariant._ // for imap
  //  import cats.syntax.semigroup._ // for |+|
  //
  //  implicit val symbolMonoid: Monoid[Symbol] =
  //    Monoid[String].imap(Symbol.apply)(_.name)
  //
  //  Monoid[Symbol].empty
  //  // res3: Symbol = '
  //
  //  Symbol("a") |+| Symbol("few") |+| Symbol("words")
  //  // res4: Symbol = 'afewwords

  /* 3.7 Aside: Partial Unification(余談：部分的な統一
  * セクション3.2では、Function1のファンクターインスタンスを見ました。
  * */

  //  import cats.Functor
  //  import cats.instances.function._ // for Functor
  //  import cats.syntax.functor._     // for map
  //
  //  val func1 = (x: Int)    => x.toDouble
  //  val func2 = (y: Double) => y * 2
  //
  //  val func3 = func1.map(func2)

  //Function1には、2つの型パラメーター（関数の引数と結果の型）があります。
  //  trait Function1[-A, +B] {
  //    def apply(arg: A): B
  //  }

  //ただし、Functorは、次の1つのパラメーターを持つ型コンストラクターを受け入れます。
  //  trait Functor[F[_]] {
  //    def map[A, B](fa: F[A])(func: A => B): F[B]
  //  }

  //コンパイラーは、Function1の2つのパラメーターのいずれかを修正して、
  // Functorに渡す正しい種類の型コンストラクターを作成する必要があります。次の2つのオプションから選択できます。

  //type F[A] = Int => A
  //type F[A] = A => Double

  /* これらの前者が正しい選択であることを私たちは知っています。
    ただし、コンパイラはコードの意味を理解していません。
    代わりに、「部分的な統合」と呼ばれるものを実装する単純なルールに依存しています。
    Scalaコンパイラーの部分的な統合は、型パラメーターを左から右に修正することで機能します。
    上記の例では、コンパイラはInt => DoubleのIntを修正し、タイプInt =>？の関数のファンクターを探します。*/

  //  type F[A] = Int => A
  //
  //  val functor = Functor[F]

  //この左から右への除去は、Function1やEitherなどのタイプのファンクターを含むさまざまな一般的なシナリオで機能します。

  val either: Either[String, Int] = Right(123)
  // either: Either[String, Int] = Right(123)

  either.map(_ + 1)

  /* 3.7.1 Limitations of Partial Unification(部分的統合の制限)
  * 左から右への除去が正しい選択ではない状況があります。 1つの例は、ScalacticのOrタイプです。これは、従来は左バイアスのいずれかと同等です。
  * https://www.scalactic.org/
  * */
  //  type PossibleResult = ActualResult Or Error

  /* もう1つの例は、Function1の共変ファンクターです。
  Function1の共変FunctorはandThenスタイルの左から右への関数合成を実装しますが、
  Contravariantファンクターはcomposeスタイルの右から左への合成を実装します。つまり、次の式はすべて同等です。*/

  //  val func3a: Int => Double =
  //    a => func2(func1(a))
  //
  //  val func3b: Int => Double =
  //    func2.compose(func1)
  //
  //  // Hypothetical example. This won't actually compile:
  //  val func3c: Int => Double =
  //    func2.contramap(func1)

  //ただし、これを実際に試してみると、コードはコンパイルされません。
  //  import cats.syntax.contravariant._ // for contramap
  //
  //  val func3c = func2.contramap(func1)
  //  // error: value contramap is not a member of Double => Double
  //  // val func3c = func2.contramap(func1)
  //  //

  /* ここでの問題は、Function1のContravariantが戻り値の型を修正し、パラメーター型を変化させたままにすることです。
  * コンパイラは、単に左から右へのバイアスのために失敗します。
  * Function1のパラメーターを反転するタイプエイリアスを作成することで、これを証明できます。
  * */

  type <=[B, A] = A => B

  type F[A] = Double <= A

  /* <=のインスタンスとしてfunc2を再入力すると、必要な削除順序がリセットされ、必要に応じてコントラマップを呼び出すことができます。 */

  //  val func2b: Double <= Double = func2
  //
  //  val func3c = func2b.contramap(func1)

  /* func2とfunc2bの違いは純粋に構文上のものです。
  どちらも同じ値を参照し、それ以外の場合はタイプエイリアスは完全に互換性があります。
  しかし、信じられないほど、この単純な言い換えは、問題を解決するために必要なヒントをコンパイラーに与えるのに十分です。
  このような右から左への消去を行う必要があることはめったにありません。
  ほとんどのマルチパラメーター型コンストラクターは右バイアスされるように設計されており、
  コンパイラーがすぐにサポートする左から右への除去が必要です。
  ただし、上記のような奇妙なシナリオに遭遇した場合に備えて、この除去順序の癖について知っておくと便利です。

  3.8 Summary
  ファンクターは、シーケンス動作を表します。
  この章では、次の3種類のファンクターについて説明しました。
  - 通常の共変ファンクターは、マップメソッドを使用して、あるコンテキストで関数を値に適用する機能を表します。
    mapを連続して呼び出すと、これらの関数が順番に適用され、それぞれが前の関数の結果をパラメーターとして受け入れます。
  - コントラバリアントファンクターは、コントラマップメソッドを使用して、関数を関数のようなコンテキストに「追加」する機能を表します。
    コントラマップを連続して呼び出すと、これらの関数がマップするのとは逆の順序でシーケンスされます。
  - invariantファンクターは、imapメソッドを使用して、双方向変換を表します。

  通常のファンクターは、これらの型クラスの中で群を抜いて最も一般的ですが、それでも、それらを単独で使用することはめったにありません。
  ファンクターは、私たちが常に使用するいくつかのより興味深い抽象化の基本的な構成要素を形成します。
  次の章では、これらの抽象化のうちの2つ、モナドと適用可能なファンクターについて説明します。
  コレクションのファンクターは、各要素を他の要素とは独立して変換するため、非常に重要です。
  これにより、Hadoopのような「map-reduce」フレームワークで大いに活用されている手法である、
  大規模なコレクションでの変換を並列化または分散できます。
  このアプローチについては、本の後半のMap-reduceのケーススタディで詳しく説明します。
  反変型クラスと不変型クラスはあまり広く適用できませんが、変換を表すデータ型を構築するのに役立ちます。
  セミグループ型クラスについては、第6章の後半で説明するために再訪します。
*/

  def main(args: Array[String]): Unit = {

  }
}
