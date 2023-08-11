import org.apache.poi.ss.formula.functions.Finance
import kotlin.math.abs
import kotlin.math.roundToInt

const val baseRateValue: Double = 13.7
const val roEValue: Double = 2.0
const val lgdValue: Double = 45.0

const val rateChange: Double = 0.01

data class Row(
    val month: Int,
    val opening: Double,
    val interest: Int,
    val payment: Int,
    val closing: Double,
    val cof: Double,
    val coc: Double,
    val cr: Double,
)

fun main(args: Array<String>) {
    val pdValue = pdValueCalc(group = 60)
    val rate = riskPricingRate(rate = baseRateValue, term = 12, principal = 100000.0, pdValue = pdValue)
    println("Rate: $rate")
}

private fun pdValueCalc(group: Int): Double {
    return when (group) {
        in 1..60 -> 15.75
        in 61..80 -> 8.75
        else -> 5.5
    }
}

private fun riskPricingRate(rate: Double, term: Int, principal: Double, pdValue: Double): Double {
    // calculates monthly payment using pmt
    val payment = monthlyPayment(rate = rate, term = term, principal = principal).roundToInt()

    // Harmonize with the monthly repayment found above
    val rows: MutableList<Row> = mutableListOf()
    var mBalance = principal
    for (i in 1..term) {
        val openingBalance = mBalance
        val cof = costOfFund(baseRate = baseRateValue, balance = openingBalance)
        val coc = costOfCapital(roe = roEValue, balance = openingBalance)
        val cr = creditRisk(pd = pdValue, lgd = lgdValue, balance = openingBalance)
        val interest = (cof + coc + cr).roundToInt()
        mBalance -= (payment - interest)
        rows.add(
            Row(
                month = i,
                opening = openingBalance,
                interest = interest,
                payment = payment,
                closing = mBalance,
                cof = cof,
                coc = coc,
                cr = cr,
            ),
        )
    }

    // If last closing balance is less than 0 print schedule
    return if (mBalance < 0) {
        rate.also {
            println(rows.joinToString("\n"))
        }
    } else {
        riskPricingRate(
            rate = rate.plus(rateChange),
            term = term,
            principal = principal,
            pdValue = pdValue,
        )
    }
}

private fun monthlyPayment(rate: Double, term: Int, principal: Double): Double =
    abs(Finance.pmt(rate.div(12 * 100), term, principal))

private fun costOfFund(baseRate: Double, balance: Double): Double = baseRate.div(12 * 100).times(balance)
private fun costOfCapital(roe: Double, balance: Double): Double = roe.div(12 * 100).times(balance)
private fun creditRisk(pd: Double, lgd: Double, balance: Double): Double {
    val mPd = pd.div(100)
    val mlgd = lgd.div(100)
    return balance * ((mPd * mlgd) / 12)
}
