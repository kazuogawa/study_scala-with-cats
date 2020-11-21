package sandbox.Chapter1


object Chapter1 {

  //type classとも呼ばれる
  sealed trait Json

  final case class JSObject(get: Map[String, Json]) extends Json

  final case class JsString(get: String) extends Json

  final case class JsNumber(get: Double) extends Json

  final case object JsNull extends Json

  trait JsonWriter[A] {
    def write(value: A): Json
  }

  //finalは継承できない、overrideできない http://unageanu.hatenablog.com/entry/20080510/1210411810
  final case class Person(name: String, email: String)

  object JsonWriterInstances {
    //type class instances
    implicit val stringWriter: JsonWriter[String] = new JsonWriter[String] {
      override def write(value: String): Json = JsString(value)
    }
    implicit val personWriter: JsonWriter[Person] = new JsonWriter[Person] {
      override def write(value: Person): Json = JSObject(Map(
        "name" -> JsString(value.name),
        "email" -> JsString(value.email)
      ))
    }

    implicit def optionWriter[A](implicit writer: JsonWriter[A]): JsonWriter[Option[A]] =
      new JsonWriter[Option[A]] {
        override def write(option: Option[A]): Json = option match {
          case Some(aValue) => writer.write(aValue)
          case None => JsNull
        }
      }
  }

  //interface object=singleton objectにmethodを配置すること
  object Json {
    def toJson[A](value: A)(implicit w: JsonWriter[A]): Json = w.write(value)
  }

  //syntaxと呼ばれている？
  object JsonSyntax {

    implicit class JsonWriterOps[A](value: A) {
      def toJson(implicit w: JsonWriter[A]): Json = w.write(value)
    }

  }

  def implicitly[A](implicit value: A): A = value

  //implicitはobjectかtrait内に配置する必要がある

  //下記を呼ぼうとすると、どちらのimplicit valなのかわからず、implicit values errorになる。
  object ErrorImplicit {
    implicit val writer1: JsonWriter[String] = JsonWriterInstances.stringWriter
    implicit val writer2: JsonWriter[String] = JsonWriterInstances.stringWriter
  }

  //上記を発生させないようにするためには、implicitは
  //1.objectに配置
  //2.traitに配置
  //3.companion objectの型クラスに配置(companion object = 既に存在しているclassと同じ名前のobjectの事)
  //4.companion objectの型パラメータに配置(companion object = 既に存在しているclassと同じ名前のobjectの事)

  //1はimportしてscopeに入れる
  //2はextendsしてscopeに入れる
  //3と4は常に暗黙のscope内にある

  //exercise 1.3
  sealed trait Printable[A] {
    def format(value: A): String
  }

  object PrintableInstances {
    implicit val PrintableString: Printable[String] = new Printable[String] {
      override def format(value: String): String = value
    }
    implicit val PrintableInt: Printable[Int] = new Printable[Int] {
      override def format(value: Int): String = value.toString
    }
    implicit val PrintableCat: Printable[Cat] = new Printable[Cat] {
      override def format(cat: Cat): String = "%s is a %d year-old %s cat.".format(cat.name, cat.age, cat.color)
    }
  }

  //ここ答え見た
  object Printable {
    def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)

    def print[A](value: A)(implicit p: Printable[A]): Unit = println(format(value))
  }

  final case class Cat(name: String, age: Int, color: String)

  val cat: Cat = Cat("mike", 2, "white")

  object PrintableSyntax {

    implicit class PrintableOps[A](value: A) {
      def format(implicit p: Printable[A]): String = p.format(value)

      //こたえみた
      def print(implicit p: Printable[A]): Unit = println(p.format(value))

      //これだとうごかなかった
      //def print(implicit p: Printable[A]): Unit = p.print(value)
    }

  }

  //Catsでの型クラスについての扱い方の説明
  //catsのShowはこんな感じ
  //trait Show[A]{
  //  def show(value: A): String
  //}

  import cats.instances.int._
  import cats.instances.string._
  import cats.Show

  //showしたら、stringに変えられるShow関数ができたよ？みたいな認識でOK?
  val showInt: Show[Int] = Show.apply[Int]
  val showString: Show[String] = Show.apply[String]

  //これをimportすると何でもかんでもshowの関数が生える

  import cats.syntax.show._

  //stringの"123"
  val shownInt: String = 123.show

  val shownString: String = "abc".show

  //catsは型クラスごとに個別のimportを提供する。(たぶんcats.instances.int._みたいなやつだと思う

  //catsの全ての型クラスを一度にimport

  import cats._
  //全ての標準型クラスインスタンスと全ての構文をimportしたい時は下記
  //import cats.implicits._

  //特定のtypeのtraitを実装するだけで、Showのinstanceを定義できる

  import java.util.Date

  //implicit val dateShow: Show[Date] = new Show[Date]{
  //  def show(date: Date): String =
  //    s"${date.getTime}ms since the epoch."
  //}
  //上記を簡略化して書くと下記
  implicit val dateShow: Show[Date] = Show.show(date => s"${date.getTime}ms since the epoch.")

  //exercise 1.4.6
  //Printableの代わりにShowを使用してCatアプリケーションを再実装
  implicit val catShow: Show[Cat] = Show.show(cat => "%s is a %d year-old %s cat.".format(cat.name, cat.age, cat.color))

  def main(args: Array[String]): Unit = {
    //personWriterを書いていなくてもtoJsonのimplicitでpersonWriter使っていることを見つけてimportかってにしてくれる。
    import sandbox.Chapter1.Chapter1.JsonWriterInstances.personWriter
    println(Json.toJson(Person("Dave", "dave@example.com")))
    //本来はこんな感じ
    //Json.toJson(Person("Dave", "dave@example.com"))(personWriter)

    //personWriterを書いていなくてもtoJsonのimplicitでpersonWriter使っていることを見つけてimportかってにしてくれる。
    import sandbox.Chapter1.Chapter1.JsonSyntax.JsonWriterOps
    println(Person("Dave", "dave@example.com").toJson)
    //本来はこんな感じ
    //Person("Dave", "dave@example.com").toJson(personWriter)

    //同じことしているのに書き方こんなに違うのか。面白い。

    //暗黙的にいい感じのvalを持ってくる。下記だとstringWriter
    //何があるかを確認するためにデバッグに使う
    import sandbox.Chapter1.Chapter1.JsonWriterInstances.stringWriter
    println(implicitly[JsonWriter[String]])

    //本来はこんな感じ
    //implicitly[JsonWriter[String]](stringWriter)

    println(Json.toJson("A string!"))
    import sandbox.Chapter1.Chapter1.JsonWriterInstances.optionWriter
    println(Json.toJson(Option("A String")))
    //再起的にoptionWriterとstringWriterが使われるので下記のような感じ
    //Json.toJson(Option("A String"))(optionWriter(stringWriter))
    //implicitは組み合わせを検索して正しい組み合わせを見つけてくれる

    //exercise1.3の出力
    import sandbox.Chapter1.Chapter1.PrintableInstances.PrintableCat
    Printable.print(cat)

    //こたえみた。便利ですね
    import sandbox.Chapter1.Chapter1.PrintableSyntax.PrintableOps
    cat.print

    //stringを出力できるってこと？
    println(showInt.show(123))
    println(showString.show("abc"))

    println(new Date().show)

    //exercise1.4.6の出力
    println(cat.show)
  }

}
