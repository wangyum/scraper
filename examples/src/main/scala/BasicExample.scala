import scraper.config.Settings
import scraper.expressions.dsl._
import scraper.expressions.functions._
import scraper.local.LocalContext

object BasicExample {
  case class Person(name: String, gender: String, age: Int)

  def main(args: Array[String]) {
    val context = new LocalContext(Settings.load())
    val people = context lift (
      Person("Alice", "F", 9),
      Person("Bob", "M", 15),
      Person("Charlie", "M", 18),
      Person("David", "M", 13),
      Person("Eve", "F", 20),
      Person("Frank", "M", 19)
    )

    val adults = people where 'age >= 18 select ('name, 'gender)
    adults.explain()
    adults.show()

    val countGender = people groupBy 'gender agg ('gender, count() as 'count)
    countGender.explain()
    countGender.show()
  }
}
