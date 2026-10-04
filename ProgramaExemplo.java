public final class ProgramaExemplo {
    static final String TEXTO = """
        program Demo;
        const max = 10;
        var soma, i : integer;
        { comentário de bloco }
        begin
          soma := 0;
          for i := 1 to max do
            soma := soma + i;   // soma acumulada
          soma;
          Valor_Muito_Longo_Demais := 3;
          x := 5 $ 2;
        end.
        """;
}