import { Routes } from '@angular/router';
import { authGuard, adminGuard, guestGuard } from './core/guards';

export const routes: Routes = [
  { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./pages/login.component').then(m => m.LoginComponent) },
  { path: 'cadastro', canActivate: [guestGuard], data: { cadastro: true }, loadComponent: () => import('./pages/login.component').then(m => m.LoginComponent) },
  { path: 'catalogo', canActivate: [authGuard], loadComponent: () => import('./pages/catalogo.component').then(m => m.CatalogoComponent) },
  { path: 'pedidos', canActivate: [authGuard], loadComponent: () => import('./pages/pedidos.component').then(m => m.PedidosComponent) },
  { path: 'admin/produtos', canActivate: [authGuard, adminGuard], loadComponent: () => import('./pages/produtos.component').then(m => m.ProdutosComponent) },
  { path: 'admin/categorias', canActivate: [authGuard, adminGuard], loadComponent: () => import('./pages/categorias.component').then(m => m.CategoriasComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'catalogo' },
  { path: '**', redirectTo: 'catalogo' }
];
