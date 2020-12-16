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

}
