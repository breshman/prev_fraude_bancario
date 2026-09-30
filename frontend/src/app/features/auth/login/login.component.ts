import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loginForm: FormGroup = this.fb.group({
    nombreUsuario: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  isLoading = false;
  errorMessage: string | null = null;
  returnUrl = '/dashboard';

  ngOnInit(): void {
    // Si ya está autenticado, redirigir directo al dashboard
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'Credenciales inválidas. Verifica tu usuario y contraseña.';
        } else if (err.status === 0) {
          this.errorMessage = 'No se puede conectar con el servidor backend (Spring Boot en http://localhost:8080).';
        } else {
          this.errorMessage = err.error?.detail || err.error?.message || 'Error al iniciar sesión. Inténtalo nuevamente.';
        }
      }
    });
  }

  fillDemo(usuario: string, clave: string): void {
    this.loginForm.patchValue({
      nombreUsuario: usuario,
      password: clave
    });
    this.errorMessage = null;
  }
}
