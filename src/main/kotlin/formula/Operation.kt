package formula

class Operation {

    private var operators: ArrayList<Double> = ArrayList()
    private var symbols: ArrayList<Char> = ArrayList()
    private var result: Double = 0.0

    fun putOperator(operator: Double) {
        this.operators.add(operator)
    }

    fun putSymbol(symbol: Char) {
        this.symbols.add(symbol)
    }


    fun execute(): Double {
        var index = 1
        this.result = operators[0]
        this.symbols.forEach {
            when (it) {
                '+' -> {
                    this.result += operators[index]
                    index++
                }
                '-' -> {
                    this.result -= operators[index]
                    index++
                }
                '*' -> {
                    this.result *= operators[index]
                    index++
                }
                '/' -> {
                    this.result /= operators[index]
                    index++
                }
                else -> {
                    null
                }
            }
        }
        return this.result
    }

}
