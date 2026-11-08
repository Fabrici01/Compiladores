import java.util.*;

public class TesteCamada4 {
    static List<Mensagem> analisar(String fonte) {
        Varredura v = new Varredura(fonte);
        v.executar();
        return new AnalisadorSintatico(v.tokens, v.mensagens).analisar();
    }
    static long erros(List<Mensagem> ms) { return ms.stream().filter(Mensagem::ehErro).count(); }
    static void esperar(String nome, String fonte, long errosEsperados, long totalEsperado) {
        List<Mensagem> ms = analisar(fonte);
        boolean ok = erros(ms) == errosEsperados && ms.size() == totalEsperado;
        System.out.println((ok ? "OK    " : "FALHOU") + " " + nome + "  (erros=" + erros(ms) + ", total=" + ms.size() + ")");
        if (!ok) for (Mensagem m : ms) System.out.println("        " + m);
    }
    public static void main(String[] args) {
        esperar("A1", "program p; begin end.", 0, 0);
        esperar("A2",
            "program p;\n"
          + "type cor = (a, b); pt = record x, y : integer; end;\n"
          + "var i, j : integer; q : pt;\n"
          + "const n : integer = 10; m = 3;\n"
          + "procedure proc(k : integer;); begin end;\n"
          + "function f(k : integer;) : integer; var t : integer; begin t := k; end;\n"
          + "begin end.", 0, 0);
        String exemplo = ProgramaExemplo.TEXTO
            .replace("Valor_Muito_Longo_Demais := 3;", "valor := 3;")
            .replace("x := 5 $ 2;", "x := 5 + 2;");
        esperar("exemplo limpo", exemplo, 0, 1);
        esperar("exemplo original (só sintaxe)", ProgramaExemplo.TEXTO, 0, 1);
        esperar("B var x integer",   "program p; var x integer; begin end.", 1, 1);
        esperar("B texto apos fim",  "program p; begin end. x",              1, 1);
        esperar("B sem end",         "program p; begin x := 1;",             1, 1);
        esperar("B sem ; no header", "program p begin end.",                 1, 1);
        esperar("B sem program",     "begin end.",                           1, 1);
        esperar("B vazio",           "",                                     1, 1);
        esperar("B erro depois de decl", "program p; var x integer; begin y := ; end.", 2, 2);
        esperar("B lixo lexico apos fim", "program p; begin end. $", 0, 0);
        for (String s : new String[]{ "", "program", "program p;", "program p; var", "program p; type",
                "program p; const x", "program p; procedure", "end.", ".", ";;;;" }) {
            try { analisar(s); } catch (Throwable t) { System.out.println("EXCECAO em [" + s + "]: " + t); }
        }
        System.out.println("fim");
    }
}
