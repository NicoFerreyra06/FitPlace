export interface UsuarioPerfil {
  id: number;
  username: string;
  email: string;
  rol: string;
  codigoAmigo: string;
  peso: number | null;
  altura: number | null;
  imc: number | null;
  categoriaImc: string | null;
  rachaActualDias: number;
  rachaMaximaDias: number;
}
