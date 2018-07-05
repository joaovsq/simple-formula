package formula

import java.util.*
import kotlin.collections.HashMap

/**
 * A calculation is formed by one or more expressions
 */
class Calculation(
        /**
         * The full expression received
         */
        private var fullExp: String
) {

    private var variableMap: HashMap<String, Double> = HashMap()

    // declare the characters that determine a scope
    private val scopeChars = hashSetOf('(', ')', '[', ']')

    private val openScope = hashSetOf('(', '[')

    private val closeScope = hashSetOf(')', ']')

    private val matchSymbol = hashMapOf(Pair('(', ')'), Pair('[', ']'))


    /**
     * receives a variable and a value and removes all empty spaces
     */
    fun variable(c: String, value: Double): Calculation {
        variableMap[c.replace(" ", "")] = value
        return this
    }

    fun variable(c: String, value: Int): Calculation {
        variableMap[c.replace(" ", "")] = value.toDouble()
        return this
    }

    /**
     * Evaluates the expression
     */
    fun evaluate(): Double {

        while (true) {
            // checks if there is any scope in the expression
            if (symbolIsPresent(fullExp)) {

                val rawScope = extractScope(fullExp)

                val result = Expression(cleanExpression(rawScope))
                        .allVariables(this.findVariables(rawScope))
                        .execute()

                this.fullExp = this.fullExp.replace(rawScope, result.toString())

                this.variableMap[result.toString()] = result

            } else {
                // if a scope declaration is not found, then execute the fullExpression alone
                return Expression(this.fullExp)
                        .allVariables(variableMap)
                        .execute()
            }
        }

    }

    /**
     * returns the closest scope found inside the string
     *
     * First we have to find the last inner scope character ( or [
     * Then we find the first match to close the scope, starting of the inner one
     *
     */
    private fun extractScope(exp: String): String {
        // uses the position value as key and the symbol as value in the map
        val positions: HashMap<Int, Char> = HashMap()
        val begin: Int
        val end: Int

        // loop to find the opening
        for (char in this.openScope) {
            positions[exp.indexOfLast { it == char }] = char
        }

        begin = positions.keys.max()!!
        end = exp.indexOf(this.matchSymbol[positions[begin]!!]!!, begin)

        positions.clear()

        return exp.substring(
                begin,
                end + 1
        )

//        return exp.substring(
//                exp.indexOf('('),
//                exp.indexOf(')') + 1
//        )
    }

    /**
     * remove all scope symbols: ( ) [ ]
     */
    private fun cleanExpression(exp: String): String {
        var tempString: String = exp
        for (char in this.scopeChars) {
            tempString = tempString.replace(char.toString(), "")
        }
        return tempString
    }

    /**
     * finds out which variables are inside the expression
     * and returns a map
     */
    private fun findVariables(exp: String): HashMap<String, Double> {
        val variables: HashMap<String, Double> = HashMap()

        this.variableMap.forEach { entry ->
            if (exp.contains(entry.key)) {
                variables[entry.key] = entry.value
            }
        }

        return variables
    }

    /**
     * checks if any scope symbol is present in the expression
     */
    private fun symbolIsPresent(string: String): Boolean {
        for (char: Char in this.scopeChars) {
            if (string.find { it == char } != null)
                return true
        }
        return false
    }

    /**
     * Extract all variables from the expression and return them as a HashSet
     *
     * // TODO: optimize this method
     */
    fun slash(): HashSet<String> {
        val positions: SortedMap<Int, Char> = Expression.findPositions(this.fullExp, HashMap()).toSortedMap()
        val values: HashSet<String> = HashSet()

        if (positions.isEmpty()) values.add(this.fullExp)

        positions.forEach({

            // find where is the position of the key before and after the one in the for loop
            // the objective is to find the element between both keys
            var lastKey: Int? = positions.keys.findLast { p -> p < it.key }
            lastKey = lastKey ?: 0
            var nextKey: Int? = positions.keys.find { p -> p > it.key }
            nextKey = nextKey ?: this.fullExp.length

            // catch the variables before and after the symbol
            // and removes any forbidden characters
            val firstVariable = cleanExpression(this.fullExp.substring(if (lastKey == 0) lastKey else lastKey + 1, it.key))
                    .replace(" ", "")

            // verify if the variable found is a number
            // raw numbers should not be returned as variables
            if (!firstVariable.matches("-?\\d+(\\.\\d+)?".toRegex()))
                values.add(firstVariable)

            val lastVariable = cleanExpression(this.fullExp.substring(it.key + 1, nextKey))
                    .replace(" ", "")

            if (!lastVariable.matches("-?\\d+(\\.\\d+)?".toRegex()))
                values.add(lastVariable)

        })

        return values
    }
}