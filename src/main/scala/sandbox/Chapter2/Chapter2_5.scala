package sandbox.Chapter2

import cats.kernel.Monoid

object Chapter2_5 {
  //2.5.3 Monoid Syntax
  //catsはcombineメソッドの構文を|+|で提供する
  //(なんかintellijのサジェストがうまく働かないのが気になる)
  //下記importに|+|があるらしいが、cats.implicits._で代用できる
  //import cats.syntax.semigroup._

  import cats.implicits._

  val stringResult: String = "Hi " |+| "there" |+| Monoid[String].empty
  val intResult: Int = 1 |+| 2 |+| Monoid[Int].empty

  //2.5.4 Exercise
  def add(items: List[Int]): Int =
    if (items === Monoid[List[Int]].empty) Monoid[Int].empty
    else items.head |+| add(items.tail)

  //2.5.4 ans
  //普通にfold leftしちゃってる
  //sumにしろよってレコメンドがw
  def addAns(items: List[Int]): Int = items.foldLeft(0)(_ + _)

  def addAnsMonoid(items: List[Int]): Int = items.foldLeft(Monoid[Int].empty)(_ |+| _)

  //List[Option[Int]]
  //コードの重複がないように

  def add(items: List[Option[Int]]): Option[Int] = items.foldLeft(Monoid[Option[Int]].empty)(_ |+| _)

  def addAns[A](items: List[A])(implicit monoid: Monoid[A]): A = items.foldLeft(monoid.empty)(_ |+| _)

  //なんかみじかくかけるらしいが、implicitがないエラーになってしまう
  //def addAns2[A](items: List[A]): A = items.foldLeft(Monoid[A].empty)(_ |+| _)

  case class Order(totalCost: Double, quantity: Double)

  //これはoptionはいるのか・・・？
  def add(items: List[Order]): Order = items.foldLeft(Order(0, 0))((o1, o2) =>
    Order(o1.totalCost + o2.totalCost, o1.quantity + o2.quantity))

  //monoidインスタンスを定義すればいいだけらしい
  implicit val monoid: Monoid[Order] = new Monoid[Order] {
    override def empty: Order = Order(0, 0)

    override def combine(o1: Order, o2: Order): Order = Order(o1.totalCost + o2.totalCost, o1.quantity + o2.quantity)
  }

  //2.6 Applications of Monoids
  //2.6.1 Big Data
  //sparkなどのbig dataを扱う奴はほとんどMonoid

  //2.6.2 Distributed Systems
  //可換複製データのCRDT(commutative replicated data types)は、monoidを持つことに依存している

  //2.6.3 monoids in the small
  //2.7 Summary
  //こういうこともできる
  def addAll[A](values: List[A])(implicit monoid: Monoid[A]): A =
    values.foldRight(monoid.empty)(_ |+| _)

  //6
  addAll(List(1, 2, 3))

  //Some(3)
  addAll(List(None, Some(1), Some(2)))

  def main(args: Array[String]): Unit = {
    println(add(List(1, 2, 3, 4)))
    println(add(List[Int]()))
    println(add(List(Some(1), Some(2), None, Some(3), Some(4))))
    println(add(List(None)))
    //答えのaddでは、SomeのみのListの場合、型推論がうまくいかず、エラーになる
    //println(addAns(List(Some(1), Some(2), Some(3), Some(4))))
    //monoidインスタンス作ればこれでOK
    println(Order(100, 1) |+| Order(200, 2))
  }
}
