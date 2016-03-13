package frameless

import org.scalacheck.Prop
import org.scalacheck.Prop._

class JoinTests extends TypedDatasetSuite {
  test("ab.joinLeft(ac, ab.a, ac.a)") {
    def prop[A, B, C](left: List[X2[A, B]], right: List[X2[A, C]])(
      implicit
      ae: TypedEncoder[A],
      lefte: TypedEncoder[X2[A, B]],
      righte: TypedEncoder[X2[A, C]],
      joinede: TypedEncoder[(X2[A, B], Option[X2[A, C]])],
      ordering: Ordering[A]
    ): Prop = {
      val leftDs = TypedDataset.create(left)
      val rightDs = TypedDataset.create(right)
      val joinedDs = leftDs
        .joinLeft(rightDs, leftDs.col('a), rightDs.col('a))
        .collect().run().toVector.sortBy(_._1.a)

      val rightKeys = right.map(_.a).toSet
      val joined = {
        for {
          ab <- left
          ac <- right if ac.a == ab.a
        } yield (ab, Some(ac))
      }.toVector ++ {
        for {
          ab <- left if !rightKeys.contains(ab.a)
        } yield (ab, None)
      }.toVector

      (joined.sortBy(_._1.a) ?= joinedDs) && (joinedDs.map(_._1).toSet ?= left.toSet)
    }

    check(forAll { (xs: List[X2[Int, Long]], ys: List[X2[Int, String]]) => prop(xs, ys) })
  }

  test("ab.join(ac, ab.a, ac.a)") {
    def prop[A, B, C](left: List[X2[A, B]], right: List[X2[A, C]])(
      implicit
      ae: TypedEncoder[A],
      lefte: TypedEncoder[X2[A, B]],
      righte: TypedEncoder[X2[A, C]],
      joinede: TypedEncoder[(X2[A, B], X2[A, C])],
      ordering: Ordering[A]
    ): Prop = {
      val leftDs = TypedDataset.create(left)
      val rightDs = TypedDataset.create(right)
      val joinedDs = leftDs
        .join(rightDs, leftDs.col('a), rightDs.col('a))
        .collect().run().toVector.sortBy(_._1.a)

      val joined = {
        for {
          ab <- left
          ac <- right if ac.a == ab.a
        } yield (ab, ac)
      }.toVector

      joined.sortBy(_._1.a) ?= joinedDs
    }

    check(forAll { (xs: List[X2[Int, Long]], ys: List[X2[Int, String]]) => prop(xs, ys) })
  }
}
