## Simple Formula ##


Usage example:

  - Kotlin:
    
    ```
     val exp = Calculation("[([a+b]/2) + (c-d)] + (a*4)")
            .variable("a", 2)
            .variable("b", 4)
            .variable("c", 5.3)
            .variable("d", 1.5)

    println(exp.evaluate())
      
    ```
