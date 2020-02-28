package part1.state

trait RNG {
	def nextInt: (Int, RNG)
}

object RNG {

	def nonNegativeInt(rng: RNG): (Int, RNG) = {
		val (i , r) = rng.nextInt
		if (i < 0) ((i+1).abs, r)
		else (i, r)
	}

	def double(rng: RNG): (Double, RNG) = {
		val (i, r) = nonNegativeInt(rng)
		(i / (Int.MaxValue.toDouble + 1), r)
	}

	def intDouble(rng: RNG): ((Int, Double), RNG) = {
		val (i, rng2) = rng.nextInt
		val (d, r) = double(rng2)
		((i,d), r)
	}

	def doubleInt(rng: RNG): ((Double,Int), RNG) = {
		val ((i,d), r) = intDouble(rng)
		((d,i), r)
	}

	def double3(rng: RNG): ((Double, Double, Double), RNG) = {
		val (d1, r1) = double(rng)
		val (d2, r2) = double(r1)
		val (d3, r3) = double(r2)
		((d1, d2, d3), r3)
	}

	def intsTailrec(count: Int)(rng: RNG): (List[Int], RNG) = {
		@scala.annotation.tailrec
		def go(c: Int, l: List[Int], r: RNG): (List[Int], RNG) = {
			if (c > 0) {
				val (i, rr) = int(r)
				go(c-1, i::l, rr)
			}
			else (l, r)
		}
		go(count, List(), rng)
	}

	def ints(count: Int)(rng: RNG): (List[Int], RNG) = {
		if (count > 0) {
			val (h, r1) = int(rng)
			val (t, r2) = ints(count-1)(r1)
			(h :: t, r2)
		}
		else (List(), rng)
	}

	type Rand[+A] = RNG => (A, RNG)

	val int: Rand[Int] = _.nextInt

	def unit[A](a: A): Rand[A] = (a, _)

	def map[A,B](s: Rand[A])(f: A => B): Rand[B] =
		rng => {
			val (a, rng2) = s(rng)
			(f(a), rng2)
		}

	def doubleViaMap: Rand[Double] =
		map(nonNegativeInt)(_ / (Int.MaxValue.toDouble + 1))

	def map2[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A,B) => C): Rand[C] =
		rng => {
			val (a, r1) = ra(rng)
			val (b, r2) = rb(r1)
			(f(a,b), r2)
		}

	def both[A,B](ra: Rand[A], rb: Rand[B]): Rand[(A,B)] =
		map2(ra, rb)((_, _))

	def intDoubleViaBoth: Rand[(Int, Double)] =
		both(int, double)

	def doubleIntViaBoth: Rand[(Double, Int)] =
		both(double, int)

	def sequence[A](fs: List[Rand[A]]): Rand[List[A]] = fs match {
		case h :: t => map2(h, sequence(t))(_ :: _)
		case Nil => unit(List())
	}

	def sequence2[A](fs: List[Rand[A]]): Rand[List[A]] =
		fs.foldRight(unit(List[A]()))(map2(_, _)(_ :: _))

	def intsViaSequence(count: Int)(rng: RNG): Rand[List[Int]] =
		sequence(List.fill(count)(int))

	def flatMap[A,B](f: Rand[A])(g: A => Rand[B]): Rand[B] =
		rng => {
			val (a, r) = f(rng)
			g(a)(r)
		}

	def nonNegativeLessThan(n: Int): Rand[Int] =
			flatMap(nonNegativeInt)(i => {
				val mod = i % n
				if (i + (n-1) - mod >= 0) unit(mod)
				else nonNegativeLessThan(n)
			})

	def mapViaFlatMap[A,B](r: Rand[A])(f: A => B): Rand[B] =
		flatMap(r)(a => unit(f(a)))

	def map2ViaFlatMap[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A,B) => C): Rand[C] =
		flatMap(ra)(a => map(rb)(b => f(a,b)))
}


case class SimpleRNG(seed: Long) extends RNG {
	def nextInt: (Int, RNG) = {
		val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
		val nextRNG = SimpleRNG(newSeed)
		val n = (newSeed >>> 16).toInt
		(n, nextRNG)
	}
}
