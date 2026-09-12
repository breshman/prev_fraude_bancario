import { Injectable, signal } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type AppTheme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'sf_bank_theme';
  private currentThemeSubject = new BehaviorSubject<AppTheme>('light');

  /**
   * Observable del tema actual ('light' | 'dark')
   */
  public currentTheme$: Observable<AppTheme> = this.currentThemeSubject.asObservable();

  /**
   * Signal reactivo para uso directo en componentes modernos de Angular
   */
  public isDark = signal<boolean>(false);

  constructor() {
    this.initializeTheme();
  }

  /**
   * Obtiene el valor síncrono del tema actual
   */
  public get theme(): AppTheme {
    return this.currentThemeSubject.value;
  }

  /**
   * Alterna entre modo claro y modo oscuro
   */
  public toggleTheme(): void {
    const nextTheme: AppTheme = this.currentThemeSubject.value === 'dark' ? 'light' : 'dark';
    this.setTheme(nextTheme);
  }

  /**
   * Establece un tema específico y lo persiste
   */
  public setTheme(theme: AppTheme): void {
    this.currentThemeSubject.next(theme);
    this.isDark.set(theme === 'dark');

    if (typeof window !== 'undefined' && typeof document !== 'undefined') {
      try {
        localStorage.setItem(this.THEME_KEY, theme);
      } catch (e) {
        console.warn('No se pudo guardar la preferencia de tema en localStorage', e);
      }
      this.applyThemeToDOM(theme);
    }
  }

  private initializeTheme(): void {
    if (typeof window === 'undefined' || typeof document === 'undefined') {
      return;
    }

    // 1. Verificar preferencia guardada en localStorage
    let savedTheme: string | null = null;
    try {
      savedTheme = localStorage.getItem(this.THEME_KEY);
    } catch (e) {
      // Ignorar si el storage está bloqueado
    }

    if (savedTheme === 'light' || savedTheme === 'dark') {
      this.setTheme(savedTheme as AppTheme);
      return;
    }

    // 2. Si no hay preferencia guardada, verificar preferencia del sistema operativo
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    const initialTheme: AppTheme = prefersDark ? 'dark' : 'light';
    this.setTheme(initialTheme);

    // 3. Escuchar cambios en la preferencia del sistema operativo
    if (window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        if (!localStorage.getItem(this.THEME_KEY)) {
          this.setTheme(e.matches ? 'dark' : 'light');
        }
      });
    }
  }

  private applyThemeToDOM(theme: AppTheme): void {
    const root = document.documentElement;
    root.setAttribute('data-bs-theme', theme);
  }
}
