package scraper.expressions

import scraper.{Name, Row}
import scraper.expressions.typecheck.{PassThrough, TypeConstraint}
import scraper.types._

case class CreateNamedStruct(names: Seq[Expression], values: Seq[Expression]) extends Expression {
  assert(names.length == values.length)

  override def isNullable: Boolean = false

  override def children: Seq[Expression] = names ++ values

  override def nodeName: String = "NAMED_STRUCT"

  override def evaluate(input: Row): Any = Row.fromSeq(values map (_ evaluate input))

  override protected lazy val typeConstraint: TypeConstraint =
    (names sameTypeAs StringType andThen (
      _.forall(_.isFoldable),
      "Struct field names must be constants."
    )) ++ PassThrough(values)

  override protected lazy val strictDataType: DataType = {
    val fields = (evaluatedNames, values map (_.dataType), values map (_.isNullable)).zipped map {
      (name, dataType, nullable) => StructField(Name.caseInsensitive(name), dataType, nullable)
    }

    StructType(fields)
  }

  override protected def template(childList: Seq[String]): String = {
    val (nameStrings, valueStrings) = childList splitAt names.length
    val argStrings = nameStrings zip valueStrings flatMap { case (name, value) => Seq(name, value) }
    argStrings mkString (s"$nodeName(", ", ", ")")
  }

  private lazy val evaluatedNames: Seq[String] =
    names map (_.evaluated match { case n: String => n })
}

case class CreateArray(values: Seq[Expression]) extends Expression {
  assert(values.nonEmpty)

  override def isNullable: Boolean = false

  override def children: Seq[Expression] = values

  override def nodeName: String = "ARRAY"

  override def evaluate(input: Row): Any = values map (_ evaluate input)

  override protected lazy val typeConstraint: TypeConstraint = values.sameType

  override protected lazy val strictDataType: DataType =
    ArrayType(values.head.dataType, values exists (_.isNullable))
}
