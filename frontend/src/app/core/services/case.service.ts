import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CasoFraude, HistorialCaso, AbrirCasoRequest, CerrarCasoRequest } from '../models/case.model';

@Injectable({
  providedIn: 'root'
})
export class CaseService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/casos`;

  listarCasos(): Observable<CasoFraude[]> {
    return this.http.get<CasoFraude[]>(this.baseUrl);
  }

  abrirCaso(request: AbrirCasoRequest): Observable<CasoFraude> {
    return this.http.post<CasoFraude>(this.baseUrl, request);
  }

  agregarEvento(idCaso: number, tipoEvento: HistorialCaso['tipoEvento'], comentario: string): Observable<HistorialCaso> {
    return this.http.post<HistorialCaso>(`${this.baseUrl}/${idCaso}/historial`, {
      tipoEvento,
      comentario
    });
  }

  cerrarCaso(idCaso: number, request: CerrarCasoRequest): Observable<CasoFraude> {
    return this.http.patch<CasoFraude>(`${this.baseUrl}/${idCaso}/cerrar`, request);
  }
}
