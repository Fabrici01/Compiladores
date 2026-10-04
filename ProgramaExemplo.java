public final class ProgramaExemplo {
    static final String TEXTO = """
        program Demo;
        type
          cor = (vermelho, verde, azul, verde);
          ponto = record
            x, y : integer;
          end;
        var soma, i : integer;
            p : ponto;
            c : cor;
        const max = 10;
        { comentário de bloco }
        begin
          soma := 0;
          p.x := 3;
          for i := 1 to max do
            soma := soma + i;   // soma acumulada
          soma;
          Valor_Muito_Longo_Demais := 3;
          x := 5 $ 2;
        end.
        """;
}