package formula

import io.reactivex.Single

/**
 * TODO: optimization
 *
 * A class to create and read a expression
 *
 * Available operators are: * + - /
 * Expressions may only contain chars as variables
 *
 * OBS: A expression does not support scopes, if you need to use scopes then use the Calculation object
 *
 */
class Expression(exp: String) {

    private val exp: String
    private var variableMap: HashMap<String, Double> = HashMap()
    private val RANDOM: String = "R"

    init {
        this.exp = exp.replace(" ", "")
    }

    fun allVariables(variableMap: HashMap<String, Double>): Expression {
        this.variableMap = variableMap
        return this
    }

    fun variable(c: String, value: Double): Expression {
        variableMap[c.replace(" ", "")] = value
        return this
    }

    fun variable(c: String, value: Int): Expression {
        variableMap[c.replace(" ", "")] = value.toDouble()
        return this
    }

    /**
     * Executes the expression and returns the result
     */
    fun execute(): Double {

        val resultKey = analyse(this.exp, { it == '*' || it == '/' })
                .flatMap {
                    return@flatMap analyse(it, { it == '+' || it == '-' })
                }
                .blockingGet()

        return variableMap[resultKey ?: this.exp]!!
    }

    /**
     * Find all sub expressions containing multiplications and divisions
     */
    private fun analyse(tempExp: String, predicate: (Char) -> Boolean): Single<String> {
        var symbolIndex = -1
        var counter = 0
        return Single.just(tempExp)
                .map { fullExp ->
                    var exp = fullExp
                    exp.filter { predicate.invoke(it) }
                            .forEach {
                                var positions: HashMap<Int, Char> = HashMap()

                                // TODO: find a better way to implement this later, it is too slow now
                                positions = findPositions(exp, positions)

                                // check if the previous index is equal
                                val tempI = exp.indexOf(it, symbolIndex + 1)
                                symbolIndex = if (tempI == -1) exp.indexOf(it, 0) else tempI

                                var lastPosition: Int? = positions.keys.findLast { p -> p < symbolIndex }
                                var nextPosition: Int? = positions.keys.find { p -> p > symbolIndex }

                                lastPosition = lastPosition ?: 0
                                nextPosition = nextPosition ?: exp.length

                                val firstVariable = exp.substring(if (lastPosition == 0) lastPosition else lastPosition + 1, symbolIndex)
                                val lastVariable = exp.substring(symbolIndex + 1, nextPosition)
                                val symbol = exp[symbolIndex]

                                val builder: StringBuilder = StringBuilder()
                                        .append(firstVariable)
                                        .append(symbol)
                                        .append(lastVariable)

                                var operation = Operation()

//                                operation.putOperator(this.variableMap[firstVariable] ?: 0.0)
//                                operation.putOperator(this.variableMap[lastVariable] ?: 0.0)

                                // when finding the operators first we have to check if the variables exists in the map
                                // if they don't, then we need to check if they are the number itself
                                // if they are not in the map and are not the number itself, then we use the default number 0.0
                                operation.putOperator(this.variableMap[firstVariable]
                                        ?: if (firstVariable.matches("-?\\d+(\\.\\d+)?".toRegex())) firstVariable.toDouble() else 0.0)

                                operation.putOperator(this.variableMap[lastVariable]
                                        ?: if (lastVariable.matches("-?\\d+(\\.\\d+)?".toRegex())) lastVariable.toDouble() else 0.0)

                                operation.putSymbol(symbol)

                                // uses a unique identifier for each result and store it in the hashmap
                                val id = this.RANDOM + counter++
                                this.variableMap[id] = operation.execute()

                                exp = exp.replaceFirst(builder.toString(), id)
                            }
                    return@map exp
                }
    }

    companion object {

        /**
         * Map the special characters positions
         */
        fun findPositions(tempExp: String, positions: HashMap<Int, Char>): HashMap<Int, Char> {
            var index = -1
            // find all symbols positions in the string and map them
            tempExp.filter { it == '*' || it == '/' || it == '-' || it == '+' }.forEach {
                index = tempExp.indexOf(it, index + 1)
                positions[index] = it
            }
            return positions
        }

    }

}