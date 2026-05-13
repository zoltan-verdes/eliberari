import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path:'',
        pathMatch:'full',
        loadComponent: () => import('./incarca-lot/incarca-lot').then(m => m.IncarcaLot)
    },
    {
        path:'scanat',
        loadComponent: () => import('./incarca-scanat/incarca-scanat').then(m => m.IncarcaScanat) 
    },
    {
        path:'dovezi',
        loadComponent: () => import('./dovezi-ridicare/dovezi-ridicare').then(m => m.DoveziRidicare) 
    }

];
