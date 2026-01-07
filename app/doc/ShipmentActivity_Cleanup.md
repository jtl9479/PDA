# ShipmentActivity.java 정리 기록

## 작업 일자
2026-01-06

## 개요
ShipmentActivity.java에서 불필요한 주석 처리된 코드와 미사용 변수를 삭제하였습니다.

---

## 1. 삭제된 주석 처리 코드 (1차 작업)

### onCreate() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 386행 | `//sp_center_name.setSelection(0);` |
| 393행 | `//sp_center_name.setOnItemClickListener(centerItemClickedListener);` |
| 399행 | `//sp_product_code = (Spinner) findViewById(R.id.sp_product_code);` |
| 451행 | `//new ProgressDlgShipSelect(ShipmentActivity.this).execute();` |

### inputBtnListener 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 524-525행 | 박스 훼손 관련 주석 및 `//expiryDayTrans = "";` |
| 528행 | `//Toast.makeText(getApplicationContext(), "바코드를 스캔하세요,", Toast.LENGTH_SHORT).show();` |
| 544행 | `//double weight_double = Math.round(Double.parseDouble(weight_str) * 100) / 100.0;` |
| 622행 | `//Log.i(TAG, "=====================팝업 닫아야 진행되나?==================");` |

### sendBtnListener 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 669-681행 | 전송 카운트 체크 관련 주석 블록 (iCount, arSM 순회, btn_send 비활성화) |

### initBtnListener 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 753-754행 | `//toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);` 및 `//toast.show();` |

### mHandler 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 799행 | `//myAdapter.notifyDataSetChanged();` |

### setBarcodeMsg() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 990-998행 | 중복 체크 관련 주석 블록 |
| 1005-1008행 | work_ppcode 및 ProgressDlgShipSelect 호출 주석 |
| 1025-1047행 | 중복상품스캔 AlertDialog 주석 블록 |
| 1052, 1058행 | `//scanFlag_swap();` |
| 1077-1078행 | `//scanFlag_swap();` 및 `//set_scanFlag(false);` |
| 1038행 | `//if (find_BL(msg))` 주석 |
| 1042행 | `//0216 수정` |
| 1069-1105행 | 소비기한 변조 관련 큰 주석 블록 (약 36줄) |
| 1081행 | `//show_sendFinishDialog();` |
| 1092행 | `//scanFlag_swap();` |
| 1096행 | `//show_wetNextDialog();` |
| 1122행 | `//set_scanFlag(true);` |
| 1126행 | `//scanFlag_swap();` |
| 1228-1229행 | `//item_weight_double = Math.floor...` 관련 2줄 |

### wet_data_insert() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 1377행 | `//show_wetNextDialog();` |
| 1447행 | `//arSM.get(current_work_position).setGI_QTY(...)` 원래 소스 주석 |
| 1514행 | `//show_wetNextDialog();` |

### find_PackerProduct() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 1546행 | `//String pp_name = "";` |
| 1554행 | `//scanFlag_swap();` |

### find_PackerProductBarcodeGoods() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 1562행 | `//String pp_name = "";` |

### find_work_info() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 1613행 | `//pp_name = bi.getITEM_NAME_KR();` |
| 1623행 | `//work_ppcode = bi.getPACKER_PRODUCT_CODE();` |
| 1642행 | `//work_ppcode = bi.getPACKER_PRODUCT_CODE();` |
| 1647-1660행 | searchType "5" 관련 주석 블록 (14줄) |

---

## 2. 삭제된 주석 처리 코드 (2차 작업)

### setPrinting() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 1975-1982행 | 패커프로덕코드, 소비기한 정보 Log 주석 블록 (8줄) |
| 1988-1989행 | `//int from = ...` 및 `//int to = ...` |
| 2021-2024행 | `//int from = ...`, `//int to = ...`, `// String gubunja = ...` |
| 2031행 | `// int shelfLiftToInt = ...` 소비기한계산일 주석 |
| 2069행 | `//String pItemCode = "";` |
| 2071-2078행 | if(si.getITEM_TYPE()...) 관련 주석 블록 |
| 2077-2079행 | `//print_weight_double = ...`, `//weight_double = Math.round...` |
| 2091행 | `//pWeight = si.getPACKWEIGHT();` |
| 2109-2110행 | 테스트용코드 `//si.setBARCODE_TYPE("M3");` |
| 2221-2222행 | `//pBarcode = hMap.get(...)` 관련 주석 |

### setHomeplusPrinting() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 2381-2385행 | EMARTLOGIS_CODE 출력 관련 주석 블록 |
| 2406-2412행 | EMARTLOGIS_NAME 출력 관련 주석 블록 |
| 2562행 | `//byteStream.write(WoosimCmd.PM_setPosition(383, 270));` |
| 2720행 | `//byteStream.write(WoosimCmd.PM_setPosition(383, 270));` |
| 2794-2796행 | `//print_weight_double = ...`, `//weight_double = Math.round...` |
| 2814-2825행 | pointName 길이별 출력 위치 주석 블록 (12줄) |

### setPrintingLotte() 메서드 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 2903행 | `//String pItemCode = "";` |
| 3001-3002행 | `//tring boxserial_cnt = ...` 및 바코드 중복건 관련 주석 |
| 3126행 | `//byteStream.write(WoosimCmd.PM_setPosition(383, 270));` |

### ProgressDlgShipmentSend AsyncTask 관련
| 원래 위치 | 삭제된 코드 |
|-----------|-------------|
| 3648행 | 출하대상 리스트 관련 긴 주석 |
| 3649행 | `//result = HttpHelper.getInstance().sendData(...)` |
| 3684-3688행 | 출하대상 Table 업데이트 관련 주석 및 `//receiveData = HttpHelper...` |
| 3771행 | `//result = HttpHelper.getInstance().sendDataDb(...)` |
| 3812-3816행 | 출하대상 Table 업데이트 관련 주석 및 `//receiveData = HttpHelper...` |

---

## 3. 삭제된 미사용 변수

| 변수명 | 타입 | 원래 위치 | 삭제 사유 |
|--------|------|-----------|-----------|
| `fullbarcode` | String | 900행 | 선언만 되고 전혀 사용되지 않음 |

---

## 4. 확인 필요 사항 (코드 리뷰 필요)

### `if (true)` 조건문 (1034행)
- **위치**: setBarcodeMsg() 메서드 내 BL스캔 처리 부분
- **현재 상태**: `if (true) {` - 항상 참인 조건
- **원래 코드**: `if (find_BL(msg)) {` - find_BL 메서드 호출이 주석 처리됨
- **영향**: BL번호 유효성 검증이 생략됨
- **권장 조치**: find_BL(msg) 메서드 호출 복원 또는 조건문 완전 제거 검토 필요

### `finish_flag` 변수
- **위치**: 313행에 선언
- **현재 상태**: 값이 설정(4438행, 4506행)되기만 하고 읽히지 않음
- **권장 조치**: 삭제 검토 필요

---

## 5. 복원된 설명 주석 (오삭제 정정)

아래 항목들은 **설명 주석**(descriptive comments)으로, 주석 처리된 코드가 아닌 코드 설명을 위한 주석입니다.
1차/2차 작업 중 오삭제되어 복원하였습니다.

| 위치 | 복원된 주석 | 복원 사유 |
|------|-------------|-----------|
| 447행 | `// 출하대상 불러오기 끝, Print 연결 시작` | 섹션 구분 설명 주석 |
| 2034행 | `//쉘프라이프를 더한다.` | 로직 설명 주석 |
| 2039행 | `//dash 처리 위해서 잘라서 다시 붙임` | 로직 설명 주석 |
| 3133행 | `// 겉 테두리` | 인라인 설명 주석 |
| 3135행 | `// 원앤원` | 조건 설명 주석 |

---

## 6. 요약

### 1차 작업
- **삭제된 주석 라인**: 약 80+ 줄
- **삭제된 미사용 변수**: 1개 (`fullbarcode`)

### 2차 작업
- **setPrinting() 주석 삭제**: 약 30줄
- **setHomeplusPrinting() 주석 삭제**: 약 20줄
- **setPrintingLotte() 주석 삭제**: 약 10줄
- **AsyncTask 주석 삭제**: 약 15줄

### 총계
- **삭제된 주석 라인**: 약 155줄
- **삭제된 미사용 변수**: 1개
- **예상 파일 크기 감소**: 약 6-8KB
