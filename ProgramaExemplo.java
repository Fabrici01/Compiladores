public final class ProgramaExemplo {
    static final String TEXTO = """
       program demo;
       type
        dia = (seg, ter, qua, ter);
        aluno = record
          nome, nota, : integer;
        end;

      var a, b, contador : integer;
          al : aluno;
          d : dia;
      const limite = 5;
      { comentario de bloco }
      begin
      a := 0;
      al.nota := 7;
      while a < limite do
        a := a + 1;     // incrementa
      b := a # 3;
      Identificador_Extremamente_Longo_Demais_Para_O_Lexico := 1;
      if a > 2
        b := 10
      else
        b := ;
      contador := (a + b;
      a + b;
      c := 'texto sem fim;
      b := 3 @ 4;
      b := 12abc;
    end.
        """;
}
