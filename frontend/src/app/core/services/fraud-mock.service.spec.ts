import { TestBed } from '@angular/core/testing';

import { FraudMockService } from './fraud-mock.service';

describe('FraudMockService', () => {
  let service: FraudMockService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FraudMockService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
