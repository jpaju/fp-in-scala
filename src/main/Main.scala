object Main {
	def main(args: Array[String]): Unit = {
		val intList = introduction.List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
		val doubleList = introduction.List(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

		val x = introduction.List.zipWith(introduction.List(1, 2, 3), introduction.List(3, 2, 1))(_ + _)
		println(x)
	}

}
