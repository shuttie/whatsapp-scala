import com.github.shuttie.swhatsapp.{ProtocolNode, BinTreeNodeWriter, KeyStream, BinHex}
import com.github.shuttie.swhatsapp.protocol.Auth
import org.scalatest.{WordSpecLike, MustMatchers}

/**
 * Created by shutty on 3/23/15.
 */
class WriterSpec extends WordSpecLike with MustMatchers {
  "Writer" must {
    "serialize fine" in {
      val auth = Auth("123456789", "nb97ZaxkMrX5nNDuEb8H2o4SBJI=", BinHex.hex2bin("17a92a95be51f28b8eb5832f31b974207b22ff60"))
      val key = auth.outputKey
      val writer = new BinTreeNodeWriter
      writer.setKey(key)
      val node = ProtocolNode("presence", Map("name" -> "Test Account"))
      val data = writer.write(node, true)
      assert(BinHex.bin2hex(data) == "80001622ef2300f48212f0b22cc1503e6f10493558c79ef30d")
    }
  }
}
