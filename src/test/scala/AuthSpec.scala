import com.github.shuttie.swhatsapp.ProtocolNode
import com.github.shuttie.swhatsapp.protocol.Auth
import org.scalatest.{MustMatchers, WordSpecLike}

/**
 * Created by shutty on 3/23/15.
 */
class AuthSpec extends WordSpecLike with MustMatchers {
  val cellPhone = "79103408586"
  val password = "X1E5cALKPQbcZ5TDmACqF5KsV98="
  val challenge = Array(-47,12,65,-28,-47,39,-4,74,95,41,31,47,19,-46,55,102,-112,-8,39,38).map(_.toByte)
  "Auth" must {
    val auth = Auth(cellPhone, password, challenge)
    "generate correct keys" in {
      val keys = List(
        Array(113,-111,92,9,-34,62,10,63,-1,80,-91,44,73,-16,33,122,118,71,-74,12).map(_.toByte),
        Array(-100,116,120,38,-124,-35,-86,-99,-125,26,-81,-62,31,20,-4,-34,13,-105,-115,84).map(_.toByte),
        Array(-4,22,-15,61,-9,29,-18,20,-89,-97,13,31,-121,-56,21,105,-53,-108,-127,2).map(_.toByte),
        Array(-29,24,-69,106,73,114,46,21,107,103,-45,-85,-119,92,87,72,-79,97,23,94).map(_.toByte)
      )
      assert(auth.keys(0).sameElements(keys(0)))
      assert(auth.keys(1).sameElements(keys(1)))
      assert(auth.keys(2).sameElements(keys(2)))
      assert(auth.keys(3).sameElements(keys(3)))
    }
    "generate correct auth response 1" in {
      val resp = Array(-118,28,47,-78,63,113,-103,-25,-126,87,-89,96,54,67,-112,120,26,68,-2,10,-45,75,-37,85,17,-17,-118,53,16,-65,-48,23,14,-57,59).map(_.toByte)
      assert(auth.response.sameElements(resp))
    }
    "generate correct auth response 2" in {
      val ch = Array(0,-74,3,66,67,23,30,114,119,118,4,121,-47,-77,-117,-101,-52,31,-124,-10).map(_.toByte)
      val xauth = Auth(cellPhone, password,ch)
      val node = ProtocolNode("response", data = xauth.response)
      assert(node.toString == """<response>9c656d27f20488c51101b8090945595f27ef80ac28dad42404743c391991018ec1788e</response>""")
    }
  }
}
