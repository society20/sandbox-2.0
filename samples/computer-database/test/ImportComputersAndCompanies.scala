import java.text.DateFormat._
import java.text.{ DateFormat, SimpleDateFormat }
import java.util.Date
import java.util.Locale
import com.mongodb.casbah.Imports._
import scala.io.Source

object ImportComputersAndCompanies {

  def main(args: Array[String]): Unit = {
    val mongoConn = MongoConnection();

    val computersColl = mongoConn("cdb")("computers")
    val companiesColl = mongoConn("cdb")("companies")

    var companyMap = Map[String, Object]();

    val companyFile = Source.fromFile("test/companies.txt")
    for (line <- companyFile.getLines()) {

      val array = line.split(',')
      val companyId = array(0) trim
      val companyName = array(1) trim

      val company = MongoDBObject.newBuilder
      company += "name" -> companyName

      val oneCia = company.result()
      companiesColl.insert(oneCia)

      val oid = oneCia.get("_id")
      companyMap += companyId -> oid
      println(oneCia)
    }
    println("----------------------------------------")
    //import computers
    val computersFile = Source.fromFile("test/computers.txt")
    val formatter = new SimpleDateFormat("yyyy-MM-dd")
    for (jsonBook <- computersFile.getLines()) {
      val array = jsonBook.split(',')
      val computerName = array(1) trim
      val rawIntroduced = array(2) trim
      val rawDiscontinued = array(3).trim
      val companyId = array(4) trim

      val onePc = MongoDBObject.newBuilder
      onePc += "name" -> computerName

      if (rawIntroduced != "null") {

        val introduced = rawIntroduced;
        val date = formatter.parse(introduced)
        onePc += "introduced" -> date
      }

      if (rawDiscontinued != "null") {
        val discontinued = rawDiscontinued;
        val date = formatter.parse(discontinued)
      }

      if (companyId != "null") {
        val oid = companyMap(companyId)
        onePc += "company_id" -> oid
      }

      val mongoEntry = onePc.result()
      computersColl.insert(mongoEntry)
      println(mongoEntry)
    }
    println("done")
  }

}