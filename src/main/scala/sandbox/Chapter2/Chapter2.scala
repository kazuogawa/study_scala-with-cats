package sandbox.Chapter2


object Chapter2 {

  object Chapter2_1 {
    //monoidとsemigroupの説明
    //trait Monoid[A] {
    //  def combine(x: A, y: A): A

    //  def empty: A
    //}

    //monoidは下記ふたつの結合則に従わなければならない
    //右から結合しても左から結合しても同じ値になること
    def associativeLaw[A](x: A, y: A, z: A)(implicit m: Monoid[A]): Boolean = {
      m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)
    }

    //空と結合しても値が変わらないこと
    def identityLaw[A](x: A)(implicit m: Monoid[A]): Boolean = {
      (m.combine(x, m.empty) == x) && (m.combine(m.empty, x) == x)
    }

    //例えば整数の減算は結合法則ではないのでモノイドではない
    //(1 - 2) - 3

    //semigroupはcombineのみでemptyがないmonoidの結合部分のみのことを言う
    trait Semigroup[A] {
      def combine(x: A, y: A): A
    }

    //catのMonoidのより正確な定義は下記
    trait Monoid[A] extends Semigroup[A] {
      def empty: A
    }

    //monoidを定義するともれなくsemigroupも取得できる
    //semigroupを求めているmethodに代わりにmonoidを渡すこともできる

    //exercise 2.3
    //object Monoid {
    //  def apply[A](implicit monoid: Monoid[A]): Monoid[A] = monoid
    //}

    //問題の意味がわからず、答えを見た。Booleanの結合則を満たす関数について実装すればよかったらしい
    //4つある

    implicit val booleanAndMonoid: Monoid[Boolean] =
      new Monoid[Boolean] {
        override def empty: Boolean = true

        override def combine(x: Boolean, y: Boolean): Boolean = x && y
      }

    implicit val booleanOrMonoid: Monoid[Boolean] =
      new Monoid[Boolean] {
        override def empty: Boolean = false

        override def combine(x: Boolean, y: Boolean): Boolean = x || y
      }

    //exclusive or
    implicit val booleanEitherMonoid: Monoid[Boolean] =
      new Monoid[Boolean] {
        override def empty: Boolean = true

        override def combine(x: Boolean, y: Boolean): Boolean = (x && !y) || (!x && y)
      }

    //exclusive nor
    implicit val booleanXnorMonoid: Monoid[Boolean] =
      new Monoid[Boolean] {
        override def empty: Boolean = false

        override def combine(x: Boolean, y: Boolean): Boolean = (!x || y) && (x || !y)
      }

    //exercise 2.4
    //setにはどんなmonoidとsemigroupがある？

    import scala.collection.immutable.Set

    //答え見た
    //和集合
    //implicit def setUnionMonoid[A]: Monoid[Set[A]] = new Monoid[Set[A]] {
    //  override def empty: Set[A] = Set.empty[A]

    //  override def combine(x: Set[A], y: Set[A]): Set[A] = x union y
    //}

    //val intSetMonoid: Monoid[Set[Int]] = Monoid[Set[Int]]
    //val strSetMonoid: Monoid[Set[String]] = Monoid[Set[String]]

    implicit def setIntersectionSemigroup[A]: Semigroup[Set[A]] = new Semigroup[Set[A]] {
      //積集合(どっちも存在しているもののみを返す)を表すintersectって関数があるんだね
      override def combine(x: Set[A], y: Set[A]): Set[A] = x intersect y
    }

    implicit def symDiffMonoid[A]: Monoid[Set[A]] = new Monoid[Set[A]] {
      override def empty: Set[A] = Set.empty

      //対象差
      override def combine(x: Set[A], y: Set[A]): Set[A] = (x diff y) union (y diff x)
    }
  }

  //2.5 monoid in cats
  //catsでの型クラス、インスタンス、インターフェースの実装をみる

  //2.5.1 the monoid type class
  //下記のようにimportするのが普通

  import cats.Monoid
  import cats.Semigroup

  //2.5.2 Monoid Instance
  //stringのmonoidインスタンスが必要であれば、下記のように書く
  import cats.instances.string._
  //下記は書き方違うが同等
  Monoid[String].combine("Hi ", "there") == Monoid.apply[String].combine("Hi ", "there")
  Monoid[String].empty == Monoid.apply[String].empty

  //emptyが必要なかったら下記でもOK
  Semigroup[String].combine("Hi ", "there")

  //intは下記

  import cats.instances.int._

  Monoid[Int].combine(32, 10)

  //option

  import cats.instances.option._

  val a: Option[Int] = Option(22)
  val b: Option[Int] = Option(20)

  //便利！
  Monoid[Option[Int]].combine(a, b)

  //型ごとにimportめんどくさかったら下記
  //import cats.implicits._



  def main(args: Array[String]): Unit = {
    //println(intSetMonoid.combine(Set(1, 2), Set(2, 3)))
    //println(strSetMonoid.combine(Set("A", "B"), Set("B", "C")))
  }
}