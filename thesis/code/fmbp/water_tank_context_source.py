class WaterTankContextSource(ContextSource):
    def get_data(self) -> dict[str, str | int | float | bool]:
        return {
            "temp":  TANK.water_temperature,
            "level": TANK.water_level,
        }